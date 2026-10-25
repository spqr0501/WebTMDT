-- Chay mot lan sau 01-schema.sql. Tai khoan duoc tao qua dang ky hoac ADMIN_EMAIL + ADMIN_PASSWORD.
USE cake_shop;
INSERT INTO danh_muc(id,ten,mo_ta) VALUES
 (1,'Bánh kem','Bánh kem tươi cho những dịp đáng nhớ'),
 (2,'Bánh mousse','Mềm mịn, thanh nhẹ, tan trong miệng'),
 (3,'Bánh cookie','Giòn thơm cho những buổi trà chiều');
INSERT INTO san_pham(ten,mo_ta,gia,ton_kho,danh_muc_id,duong_dan_anh,noi_bat,dang_ap_dung,phien_ban) VALUES
 ('Bánh kem dâu tây','Bánh kem dâu tây với cốt vanilla mềm, kem tươi và dâu. Kích thước 16 cm, phù hợp 4–6 người. Bảo quản lạnh 2–6°C. Có chứa sữa, trứng và gluten.',320000,12,1,'/images/cake.svg',TRUE,TRUE,0),
 ('Bánh mousse sô-cô-la','Mousse chocolate với hương cacao đậm, vị ngọt vừa. Kích thước 16 cm. Có chứa sữa, trứng và gluten.',350000,8,2,'/images/mousse.svg',TRUE,TRUE,0),
 ('Hộp bánh quy bơ','Hộp 12 bánh cookie bơ thơm giòn. Dùng cùng trà hoặc cà phê. Có chứa sữa, trứng và gluten.',125000,25,3,'/images/cookie.svg',TRUE,TRUE,0),
 ('Bánh kem trà xanh','Bánh kem trà xanh, cốt bánh mềm và lớp kem matcha. Kích thước 16 cm. Có chứa sữa, trứng và gluten.',380000,4,1,'/images/matcha.svg',TRUE,TRUE,0),
 ('Bánh mousse dâu mọng','Mousse dâu mọng với lớp thạch quả đỏ. Kích thước 16 cm. Có chứa sữa, trứng và gluten.',365000,7,2,'/images/berry.svg',TRUE,TRUE,0),
 ('Bánh kem chanh vàng','Bánh kem chanh vàng dịu nhẹ, thơm mát. Kích thước 16 cm. Có chứa sữa, trứng và gluten.',295000,10,1,'/images/lemon.svg',TRUE,TRUE,0);
INSERT INTO ma_giam_gia(ma,loai,gia_tri_giam,gia_tri_don_toi_thieu,giam_toi_da,bat_dau_luc,ket_thuc_luc,gioi_han_luot_dung,gioi_han_moi_nguoi,so_luot_da_dung,dang_ap_dung) VALUES
 ('WELCOME10','PERCENT',10,200000,50000,NOW(),DATE_ADD(NOW(),INTERVAL 90 DAY),100,1,0,TRUE),
 ('SWEET30','FIXED',30000,300000,NULL,NOW(),DATE_ADD(NOW(),INTERVAL 90 DAY),50,2,0,TRUE);
