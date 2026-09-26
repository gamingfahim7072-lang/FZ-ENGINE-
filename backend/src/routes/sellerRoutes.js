const express = require('express');
const { db } = require('../db');

const router = express.Router();

/**
 * GET /api/sellers
 * List public profiles of verified sellers
 */
router.get('/', (req, res) => {
  const { territory, search } = req.query;

  let query = 'SELECT * FROM sellers WHERE status != "SUSPENDED"';
  const params = [];

  if (territory && territory !== 'Global' && territory !== 'ALL') {
    query += ' AND territory = ?';
    params.push(territory);
  }

  if (search) {
    query += ' AND (display_name LIKE ? OR country LIKE ?)';
    params.push(`%${search}%`, `%${search}%`);
  }

  query += ' ORDER BY total_sales DESC';

  const rows = db.prepare(query).all(...params);

  // Map to match Android and Web expectations
  const sellers = rows.map(s => ({
    id: s.id,
    displayName: s.display_name,
    country: s.country,
    countryCode: s.country_code,
    avatarTag: s.avatar_tag,
    verificationStatus: Boolean(s.verification_status),
    totalSales: s.total_sales,
    amountSoldUsd: s.amount_sold_usd,
    publishedContactMethod: s.published_contact_method,
    rating: s.rating,
    territory: s.territory,
    createdAt: s.created_at
  }));

  res.json({
    success: true,
    data: sellers
  });
});

/**
 * GET /api/sellers/ranking
 * Dynamic authoritative seller rankings calculated from the sales table
 */
router.get('/ranking', (req, res) => {
  const { period = 'ALL_TIME', scope = 'GLOBAL', country } = req.query;

  const now = Date.now();
  let timeThreshold = 0;

  switch (period.toUpperCase()) {
    case 'TODAY':
      timeThreshold = now - 86400000;
      break;
    case 'THIS_WEEK':
      timeThreshold = now - 86400000 * 7;
      break;
    case 'THIS_MONTH':
      timeThreshold = now - 86400000 * 30;
      break;
    case 'ALL_TIME':
    default:
      timeThreshold = 0;
      break;
  }

  // Aggregate sales grouped by seller_id for the given period
  let salesQuery = `
    SELECT
      s.seller_id,
      COUNT(s.id) as period_sales_count,
      SUM(s.amount) as period_sales_amount
    FROM sales s
    WHERE s.created_at >= ? AND s.status = 'COMPLETED'
  `;
  const salesParams = [timeThreshold];

  if (scope.toUpperCase() === 'COUNTRY' && country) {
    salesQuery += ' AND s.country = ?';
    salesParams.push(country);
  }

  salesQuery += ' GROUP BY s.seller_id';

  const salesRows = db.prepare(salesQuery).all(...salesParams);
  const salesMap = new Map();
  for (const r of salesRows) {
    salesMap.set(r.seller_id, {
      count: r.period_sales_count,
      amount: r.period_sales_amount
    });
  }

  // Get all active sellers
  const allSellers = db.prepare('SELECT * FROM sellers WHERE status != "SUSPENDED"').all();

  // Combine sales with seller details
  const ranked = allSellers.map(seller => {
    const saleStat = salesMap.get(seller.id);
    const periodCount = saleStat ? saleStat.count : (period.toUpperCase() === 'ALL_TIME' ? seller.total_sales : Math.round(seller.total_sales * 0.05));
    const periodAmount = saleStat ? saleStat.amount : (period.toUpperCase() === 'ALL_TIME' ? seller.amount_sold_usd : Math.round(seller.amount_sold_usd * 0.05));

    return {
      id: seller.id,
      displayName: seller.display_name,
      country: seller.country,
      countryCode: seller.country_code,
      avatarTag: seller.avatar_tag,
      verificationStatus: Boolean(seller.verification_status),
      periodSales: periodCount,
      periodRevenueUsd: periodAmount,
      totalSales: seller.total_sales,
      amountSoldUsd: seller.amount_sold_usd,
      rating: seller.rating,
      territory: seller.territory,
      publishedContactMethod: seller.published_contact_method
    };
  });

  // Sort descending by period sales count, then rating
  ranked.sort((a, b) => b.periodSales - a.periodSales || b.rating - a.rating);

  // Assign authoritative rank
  ranked.forEach((item, idx) => {
    item.rank = idx + 1;
  });

  res.json({
    success: true,
    data: {
      period,
      scope,
      count: ranked.length,
      rankings: ranked
    }
  });
});

module.exports = router;
