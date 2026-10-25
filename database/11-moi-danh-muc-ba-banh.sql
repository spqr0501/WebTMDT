-- Chạy sau 10. Chỉ bổ sung để mỗi danh mục mẫu có ít nhất 3 bánh đang bán.
-- Không tạo bánh trung thu; không sửa giá, ảnh, tồn kho hoặc xóa bánh hiện có.
USE cake_shop;
GO
SET XACT_ABORT ON;
BEGIN TRY
 BEGIN TRANSACTION;
 DECLARE @mau TABLE(stt INT IDENTITY(1,1),danh_muc NVARCHAR(100),ten NVARCHAR(150),gia DECIMAL(14,0),anh VARCHAR(500),mo_ta NVARCHAR(1000));
 INSERT INTO @mau(danh_muc,ten,gia,anh,mo_ta) VALUES
 (N'Bánh kem',N'Bánh kem dâu tây',320000,'/images/berry.svg',N'Mẫu bánh kem vị dâu cho dịp sinh nhật.'),
 (N'Bánh kem',N'Bánh kem trà xanh',380000,'/images/matcha.svg',N'Mẫu bánh kem trà xanh, trang trí theo dịp.'),
 (N'Bánh kem',N'Bánh kem chanh vàng',295000,'/images/lemon.svg',N'Mẫu bánh kem chanh vàng dành cho buổi gặp mặt.'),
 (N'Bánh mousse',N'Bánh mousse sô-cô-la',350000,'/images/mousse.svg',N'Mẫu mousse sô-cô-la với các lớp bánh mềm.'),
 (N'Bánh mousse',N'Bánh mousse dâu mọng',365000,'/images/berry.svg',N'Mẫu mousse dâu dành cho những buổi tiệc nhỏ.'),
 (N'Bánh mousse',N'Bánh mousse chanh dây',340000,'/images/lemon.svg',N'Mousse chủ đề chanh dây, màu vàng tươi sáng.'),
 (N'Bánh quy',N'Hộp bánh quy bơ',125000,'/images/cookie.svg',N'Hộp bánh quy bơ dùng làm quà hoặc dùng cùng trà.'),
 (N'Bánh quy',N'Bánh quy hạt sô-cô-la',135000,'/images/cookie.svg',N'Hộp bánh quy chủ đề sô-cô-la, tiện chia sẻ.'),
 (N'Bánh quy',N'Bánh quy trà xanh',145000,'/images/matcha.svg',N'Hộp bánh quy trà xanh dành cho buổi trà chiều.'),
 (N'Bánh bông lan',N'Bông lan vani',95000,'/images/cake.svg',N'Mẫu bông lan vani cho bữa ăn nhẹ.'),
 (N'Bánh bông lan',N'Bông lan sô-cô-la',110000,'/images/mousse.svg',N'Mẫu bông lan vị sô-cô-la, đóng hộp mang đi.'),
 (N'Bánh bông lan',N'Bông lan chanh',105000,'/images/lemon.svg',N'Mẫu bông lan chanh cho buổi gặp bạn bè.'),
 (N'Bánh phô mai',N'Bánh phô mai cổ điển',285000,'/images/cake.svg',N'Mẫu bánh phô mai phong cách cổ điển.'),
 (N'Bánh phô mai',N'Bánh phô mai dâu',310000,'/images/berry.svg',N'Mẫu bánh phô mai trang trí chủ đề dâu.'),
 (N'Bánh phô mai',N'Bánh phô mai trà xanh',320000,'/images/matcha.svg',N'Mẫu bánh phô mai kết hợp chủ đề trà xanh.'),
 (N'Bánh su kem',N'Su kem vani',65000,'/images/cake.svg',N'Hộp su kem vị vani cho phần quà nhỏ.'),
 (N'Bánh su kem',N'Su kem sô-cô-la',75000,'/images/mousse.svg',N'Hộp su kem chủ đề sô-cô-la.'),
 (N'Bánh su kem',N'Su kem trà xanh',80000,'/images/matcha.svg',N'Hộp su kem chủ đề trà xanh cho buổi trà chiều.'),
 (N'Bánh tart',N'Tart trứng',55000,'/images/cake.svg',N'Mẫu tart trứng đóng hộp theo phần.'),
 (N'Bánh tart',N'Tart dâu tươi',85000,'/images/berry.svg',N'Mẫu tart trang trí chủ đề dâu tươi.'),
 (N'Bánh tart',N'Tart chanh',75000,'/images/lemon.svg',N'Mẫu tart chanh với sắc vàng cho bàn tiệc.'),
 (N'Bánh cuộn',N'Bánh cuộn vani',165000,'/images/cake.svg',N'Mẫu bánh cuộn vani để chia thành từng phần.'),
 (N'Bánh cuộn',N'Bánh cuộn dâu',185000,'/images/berry.svg',N'Mẫu bánh cuộn chủ đề dâu.'),
 (N'Bánh cuộn',N'Bánh cuộn trà xanh',195000,'/images/matcha.svg',N'Mẫu bánh cuộn chủ đề trà xanh.'),
 (N'Bánh sừng bò',N'Sừng bò bơ',45000,'/images/cake.svg',N'Mẫu bánh sừng bò bơ cho bữa sáng.'),
 (N'Bánh sừng bò',N'Sừng bò sô-cô-la',55000,'/images/mousse.svg',N'Mẫu bánh sừng bò chủ đề sô-cô-la.'),
 (N'Bánh sừng bò',N'Sừng bò hạnh nhân',65000,'/images/cookie.svg',N'Mẫu bánh sừng bò chủ đề hạnh nhân.'),
 (N'Bánh cupcake',N'Cupcake vani',45000,'/images/cake.svg',N'Cupcake vani cho quà tặng và tiệc nhỏ.'),
 (N'Bánh cupcake',N'Cupcake sô-cô-la',50000,'/images/mousse.svg',N'Cupcake chủ đề sô-cô-la, phần bánh nhỏ.'),
 (N'Bánh cupcake',N'Cupcake dâu',55000,'/images/berry.svg',N'Cupcake trang trí chủ đề dâu.'),
 (N'Bánh tiramisu',N'Tiramisu cà phê',285000,'/images/mousse.svg',N'Mẫu tiramisu chủ đề cà phê.'),
 (N'Bánh tiramisu',N'Tiramisu trà xanh',305000,'/images/matcha.svg',N'Mẫu tiramisu chủ đề trà xanh.'),
 (N'Bánh tiramisu',N'Tiramisu dâu',315000,'/images/berry.svg',N'Mẫu tiramisu chủ đề dâu cho dịp đặc biệt.'),
 (N'Bánh macaron',N'Hộp macaron vani',155000,'/images/cookie.svg',N'Hộp macaron chủ đề vani, làm quà nhỏ.'),
 (N'Bánh macaron',N'Hộp macaron sô-cô-la',165000,'/images/mousse.svg',N'Hộp macaron chủ đề sô-cô-la.'),
 (N'Bánh macaron',N'Hộp macaron dâu',175000,'/images/berry.svg',N'Hộp macaron chủ đề dâu với tông hồng.'),
 (N'Bánh donut',N'Donut đường',35000,'/images/cake.svg',N'Mẫu donut đường theo phần.'),
 (N'Bánh donut',N'Donut sô-cô-la',45000,'/images/mousse.svg',N'Mẫu donut chủ đề sô-cô-la.'),
 (N'Bánh donut',N'Donut dâu',45000,'/images/berry.svg',N'Mẫu donut chủ đề dâu, dùng cho tiệc nhỏ.');
 DECLARE @moi TABLE(id BIGINT,ton INT);
 DECLARE @i INT=1,@max INT=(SELECT MAX(stt) FROM @mau),@dm NVARCHAR(100),@ten NVARCHAR(150),@gia DECIMAL(14,0),@anh VARCHAR(500),@mota NVARCHAR(1000),@dm_id BIGINT;
 WHILE @i<=@max
 BEGIN
  SELECT @dm=danh_muc,@ten=ten,@gia=gia,@anh=anh,@mota=mo_ta FROM @mau WHERE stt=@i;
  SET @dm_id=NULL;
  SELECT @dm_id=id FROM dbo.danh_muc WITH(UPDLOCK,HOLDLOCK) WHERE ten=@dm;
  IF @dm_id IS NOT NULL AND (SELECT COUNT(*) FROM dbo.san_pham WHERE danh_muc_id=@dm_id AND dang_ap_dung=1)<3
    AND NOT EXISTS(SELECT 1 FROM dbo.san_pham WHERE danh_muc_id=@dm_id AND ten=@ten)
   INSERT INTO dbo.san_pham(ten,mo_ta,gia,ton_kho,danh_muc_id,duong_dan_anh,noi_bat,dang_ap_dung,phien_ban)
   OUTPUT inserted.id,inserted.ton_kho INTO @moi
   VALUES(@ten,@mota+N' Dữ liệu mẫu của cửa hàng; ảnh thực tế, thành phần, dị ứng và bảo quản sẽ được quản trị xác nhận.',@gia,12,@dm_id,@anh,0,1,0);
  SET @i=@i+1;
 END;
 INSERT INTO dbo.lich_su_kho(san_pham_id,thay_doi,ton_sau,ly_do,nguoi_thuc_hien,ngay_tao)
 SELECT id,ton,ton,N'Tồn ban đầu khi bổ sung bánh mẫu theo danh mục','SQL-11',SYSDATETIME() FROM @moi;
 SELECT COUNT(*) AS so_banh_vua_bo_sung FROM @moi;
 COMMIT TRANSACTION;
 PRINT N'Đã bổ sung bánh mẫu; không thêm bánh trung thu, không sửa sản phẩm có sẵn.';
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
 THROW;
END CATCH;
GO
