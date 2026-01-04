package com.be.server.core.admin.khachhang.repository;

import com.be.server.core.admin.khachhang.model.request.ADSearchKhachHangRequest;
import com.be.server.core.admin.khachhang.model.response.ADKhachHangResponse;
import com.be.server.entity.KhachHang;
import com.be.server.infrastructure.constant.LoaiGiayTo;
import com.be.server.repository.KhachHangRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ADKhachHangRepository extends KhachHangRepository {
    @Query(
            value = """

                                        select
                    ROW_NUMBER() OVER (ORDER BY kh.created_date DESC) AS orderNumber,
                    kh.id,
                    kh.ma,
                    kh.ho_ten,
                    kh.ngay_sinh,
                    kh.gioi_tinh,
                    kh.loai_giay_to,
                    kh.so_giay_to,
                    kh.so_dien_thoai,
                    kh.email,
                    kh.dia_chi,
                    kh.status,
                    kh.id_loai_khach_hang as idLoaiKhachHang,
                    lkh.ten as tenLoaiKhachHang
                    from khach_hang kh
                    left join loai_khach_hang lkh on lkh.id = kh.id_loai_khach_hang
                    where (:#{#request.ten} is null or kh.ho_ten like concat('%', :#{#request.ten}, '%'))
                    and (:#{#request.loaiGiayTo} is null or kh.loai_giay_to = :#{#request.loaiGiayTo})
                    and (:#{#request.soGiayTo} is null or kh.so_giay_to like concat('%', :#{#request.soGiayTo}, '%'))
                    and (:#{#request.sdtEmail} is null\s
                     or kh.so_dien_thoai like concat('%', :#{#request.sdtEmail}, '%')\s
                     or kh.email like concat('%', :#{#request.sdtEmail}, '%'))
                    and (:#{#request.status} is null or kh.status = :#{#request.status})
                    and (:#{#request.idLoaiKhachHang} IS NULL   OR (:#{#request.idLoaiKhachHang} = '-1' AND kh.id_loai_khach_hang IS NULL) OR (:#{#request.idLoaiKhachHang} > '-1' AND kh.id_loai_khach_hang = :#{#request.idLoaiKhachHang}))                                                                           \s
                    order by kh.created_date desc """,

            countQuery = """
                    select count(*)
                    from khach_hang kh
                    left join loai_khach_hang lkh on lkh.id = kh.id_loai_khach_hang
                    where (:#{#request.ten} is null or kh.ho_ten like concat('%', :#{#request.ten}, '%'))
                    and (:#{#request.loaiGiayTo} is null or kh.loai_giay_to = :#{#request.loaiGiayTo})
                    and (:#{#request.soGiayTo} is null or kh.so_giay_to like concat('%', :#{#request.soGiayTo}, '%'))
                    and (:#{#request.sdtEmail} is null\s
                     or kh.so_dien_thoai like concat('%', :#{#request.sdtEmail}, '%')\s
                     or kh.email like concat('%', :#{#request.sdtEmail}, '%'))
                    and (:#{#request.status} is null or kh.status = :#{#request.status})
                    and (:#{#request.idLoaiKhachHang} IS NULL   OR (:#{#request.idLoaiKhachHang} = '-1' AND kh.id_loai_khach_hang IS NULL) OR (:#{#request.idLoaiKhachHang} > '-1' AND kh.id_loai_khach_hang = :#{#request.idLoaiKhachHang}))                                                                           \s
                    order by kh.created_date desc """,
            nativeQuery = true
    )
    Page<ADKhachHangResponse> getAllKhachHang(Pageable pageable, ADSearchKhachHangRequest request);
    boolean existsKhachHangByEmailIgnoreCase(String email);
    boolean existsKhachHangBySoDienThoaiIgnoreCase(String sdt);

    boolean existsKhachHangByEmailIgnoreCaseAndIdNot(String email, String id);
    boolean existsKhachHangBySoDienThoaiIgnoreCaseAndIdNot(String soDienThoai, String id);

    boolean existsByLoaiGiayToAndSoGiayTo(LoaiGiayTo loaiGiayTo, String soGiayTo);
    boolean existsByLoaiGiayToAndSoGiayToAndIdNot(LoaiGiayTo loaiGiayTo, String soGiayTo,String id);
   Optional<KhachHang> findByLoaiGiayToAndSoGiayTo(LoaiGiayTo loaiGiayTo, String soGiayTo);
}
