package com.be.server.core.admin.doanluutru.service;

import com.be.server.core.admin.doanluutru.modal.request.AddMemberRequest;
import com.be.server.core.admin.doanluutru.modal.request.AddGuestDuringStayRequest;
import com.be.server.core.admin.doanluutru.modal.request.AssignRoomRequest;
import com.be.server.core.admin.doanluutru.modal.request.CreateDoanRequest;
import com.be.server.core.admin.doanluutru.modal.request.SearchMemberRequest;
import com.be.server.core.common.base.ResponseObject;

import com.be.server.entity.DoanLuuTru;


import com.be.server.core.admin.doanluutru.modal.request.FindDoanRequest;
import com.be.server.core.admin.doanluutru.modal.request.PaymentRequest;
import com.be.server.core.admin.doanluutru.modal.response.CostBreakdownResponse;
import com.be.server.core.admin.doanluutru.modal.response.InvoiceResponse;
import com.be.server.core.admin.doanluutru.modal.response.PaymentStatusResponse;

public interface AdDoanLuuTruService {
    ResponseObject<?> createDoan(CreateDoanRequest request);

    ResponseObject<?>addMember(AddMemberRequest request);

    ResponseObject<?> getMembers(String idDoanLuuTru,SearchMemberRequest request);

    DoanLuuTru getDoanByBooking(String idDatPhong);

    ResponseObject<?> getAllDoan(FindDoanRequest request);

    ResponseObject<?> getAllBookedTheoDoan(String idDoan);
   ResponseObject<?> assignRoom(String idChiTietDoan,AssignRoomRequest request);
    ResponseObject<?> checkSoluongToiDaBooking(String idDoan);

    ResponseObject<?> checkInDoanLuuTru(String idDoan);

    /**
     * Add a new guest to the group during active stay (status = DANG_LUU_TRU)
     * Validates status, guest information completeness, room assignment, and capacity
     * @param request Contains guest info and room assignment
     * @return Success or detailed validation error
     */
    ResponseObject<?> addGuestDuringStay(AddGuestDuringStayRequest request);

    /**
     * Tính tổng chi phí lưu trú cho đoàn
     * Bao gồm tiền phòng và dịch vụ phát sinh
     */
    ResponseObject<CostBreakdownResponse> calculateTotalCost(String idDoan);

    /**
     * Tạo hóa đơn tạm tính
     */
    ResponseObject<InvoiceResponse> generateTemporaryInvoice(String idDoan);

    /**
     * Xử lý thanh toán cho đoàn lưu trú
     * Hỗ trợ thanh toán một phần hoặc toàn bộ
     */
    ResponseObject<?> processPayment(PaymentRequest request);

    /**
     * Lấy trạng thái thanh toán của đoàn
     * Bao gồm tổng chi phí, đã thanh toán, công nợ, lịch sử thanh toán
     */
    ResponseObject<PaymentStatusResponse> getPaymentStatus(String idDoan);
}
