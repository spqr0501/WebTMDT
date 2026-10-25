-- Bổ sung danh mục, không thay đổi danh mục hoặc sản phẩm hiện có.
-- Có thể chạy lại; chỉ thêm tên chưa tồn tại.
USE cake_shop;
GO
SET XACT_ABORT ON;
BEGIN TRY
 BEGIN TRANSACTION;
 INSERT INTO dbo.danh_muc(ten,mo_ta)
 SELECT moi.ten,moi.mo_ta
 FROM (VALUES
  (N'Bánh bông lan',N'Bánh bông lan mềm xốp, thơm nhẹ, phù hợp bữa sáng và giờ trà.'),
  (N'Bánh phô mai',N'Các loại bánh phô mai béo mịn, kết hợp trái cây hoặc sô-cô-la.'),
  (N'Bánh su kem',N'Vỏ bánh su nhẹ, nhân kem mịn với nhiều hương vị.'),
  (N'Bánh tart',N'Đế bánh giòn với nhân trứng, kem hoặc trái cây.'),
  (N'Bánh cuộn',N'Cốt bánh mềm cuộn cùng kem, mứt hoặc trái cây.'),
  (N'Bánh sừng bò',N'Bánh ngàn lớp thơm bơ, có loại nguyên bản và các loại nhân.'),
  (N'Bánh cupcake',N'Bánh nhỏ xinh, trang trí kem, phù hợp tiệc và quà tặng.'),
  (N'Bánh tiramisu',N'Bánh vị cà phê và kem phô mai, phủ ca cao.'),
  (N'Bánh macaron',N'Bánh hạnh nhân nhỏ nhiều màu, kẹp nhân kem hoặc mứt.'),
  (N'Bánh donut',N'Bánh vòng mềm, phủ đường hoặc sô-cô-la với nhiều kiểu trang trí.')
 ) AS moi(ten,mo_ta)
 WHERE NOT EXISTS (
  SELECT 1 FROM dbo.danh_muc AS cu WITH (UPDLOCK,HOLDLOCK)
  WHERE cu.ten=moi.ten
 );
 COMMIT TRANSACTION;
 PRINT N'Đã bổ sung các danh mục bánh mới.';
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
 THROW;
END CATCH;
GO
