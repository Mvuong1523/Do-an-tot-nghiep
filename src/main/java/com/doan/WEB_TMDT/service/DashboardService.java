package com.doan.WEB_TMDT.service;

import com.doan.WEB_TMDT.dto.DashboardStatsDTO;
import com.doan.WEB_TMDT.dto.OrderDTO;

import java.util.List;

public interface DashboardService {
    DashboardStatsDTO getDashboardStats();
    List<OrderDTO> getRecentOrders(int limit);
}
