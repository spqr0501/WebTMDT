-- Chuyen database cu sang ten bang/cot tieng Viet (MySQL 8+).

-- Dung ung dung va sao luu database truoc khi chay. Chi chay MOT LAN tren schema cu.

-- Khong chay tep nay neu da tao moi bang 01-schema.sql ban tieng Viet.

-- Lenh DDL tu commit; neu bi loi giua chung, kiem tra schema truoc khi chay tiep.

USE cake_shop;

RENAME TABLE users TO nguoi_dung,
 categories TO danh_muc,
 products TO san_pham,
 addresses TO dia_chi,
 vouchers TO ma_giam_gia,
 customer_orders TO don_hang,
 order_items TO chi_tiet_don_hang,
 voucher_usages TO luot_su_dung_ma;

ALTER TABLE dia_chi
 RENAME COLUMN user_id TO nguoi_dung_id,
 RENAME COLUMN recipient TO nguoi_nhan,
 RENAME COLUMN phone TO so_dien_thoai,
 RENAME COLUMN detail TO dia_chi_chi_tiet;

ALTER TABLE danh_muc
 RENAME COLUMN name TO ten,
 RENAME COLUMN description TO mo_ta;

ALTER TABLE don_hang
 RENAME COLUMN user_id TO nguoi_dung_id,
 RENAME COLUMN status TO trang_thai,
 RENAME COLUMN payment_method TO phuong_thuc_thanh_toan,
 RENAME COLUMN recipient TO nguoi_nhan,
 RENAME COLUMN phone TO so_dien_thoai,
 RENAME COLUMN delivery_address TO dia_chi_giao_hang,
 RENAME COLUMN note TO ghi_chu,
 RENAME COLUMN subtotal TO tien_hang,
 RENAME COLUMN discount TO tien_giam,
 RENAME COLUMN shipping_fee TO phi_giao_hang,
 RENAME COLUMN total TO tong_thanh_toan,
 RENAME COLUMN voucher_code TO ma_uu_dai,
 RENAME COLUMN checkout_token TO ma_chong_dat_trung,
 RENAME COLUMN created_at TO ngay_tao;

ALTER TABLE chi_tiet_don_hang
 RENAME COLUMN order_id TO don_hang_id,
 RENAME COLUMN product_id TO san_pham_id,
 RENAME COLUMN product_name TO ten_san_pham,
 RENAME COLUMN unit_price TO don_gia,
 RENAME COLUMN quantity TO so_luong;

ALTER TABLE san_pham
 RENAME COLUMN name TO ten,
 RENAME COLUMN description TO mo_ta,
 RENAME COLUMN price TO gia,
 RENAME COLUMN stock TO ton_kho,
 RENAME COLUMN category_id TO danh_muc_id,
 RENAME COLUMN image_url TO duong_dan_anh,
 RENAME COLUMN video_url TO duong_dan_video,
 RENAME COLUMN featured TO noi_bat,
 RENAME COLUMN active TO dang_ap_dung,
 RENAME COLUMN version TO phien_ban;

ALTER TABLE nguoi_dung
 RENAME COLUMN name TO ten,
 RENAME COLUMN password TO mat_khau,
 RENAME COLUMN role TO vai_tro,
 RENAME COLUMN enabled TO duoc_hoat_dong;

ALTER TABLE ma_giam_gia
 RENAME COLUMN code TO ma,
 RENAME COLUMN type TO loai,
 RENAME COLUMN amount TO gia_tri_giam,
 RENAME COLUMN min_order TO gia_tri_don_toi_thieu,
 RENAME COLUMN max_discount TO giam_toi_da,
 RENAME COLUMN starts_at TO bat_dau_luc,
 RENAME COLUMN ends_at TO ket_thuc_luc,
 RENAME COLUMN usage_limit TO gioi_han_luot_dung,
 RENAME COLUMN per_user_limit TO gioi_han_moi_nguoi,
 RENAME COLUMN used_count TO so_luot_da_dung,
 RENAME COLUMN active TO dang_ap_dung;

ALTER TABLE luot_su_dung_ma
 RENAME COLUMN voucher_id TO ma_giam_gia_id,
 RENAME COLUMN user_id TO nguoi_dung_id,
 RENAME COLUMN order_id TO don_hang_id;
