-- Chạy sau file 07. Bổ sung dữ liệu, không xóa hay đổi trạng thái đơn cũ.
USE cake_shop;
GO
SET XACT_ABORT ON;
BEGIN TRY
 BEGIN TRANSACTION;
 IF OBJECT_ID('dbo.mau_banh_thiet_ke','U') IS NULL
 BEGIN
  CREATE TABLE dbo.mau_banh_thiet_ke (
   id BIGINT IDENTITY(1,1) PRIMARY KEY,
   banh_rieng_id BIGINT NOT NULL REFERENCES dbo.yeu_cau_banh_rieng(id),
   anh_mau VARCHAR(500) NOT NULL,
   noi_dung NVARCHAR(1500) NOT NULL,
   trang_thai VARCHAR(30) NOT NULL DEFAULT 'PENDING' CHECK(trang_thai IN ('PENDING','APPROVED','CHANGES_REQUESTED')),
   y_kien_khach NVARCHAR(1000) NULL,
   nguoi_gui VARCHAR(190) NOT NULL,
   ngay_gui DATETIME2 NOT NULL DEFAULT SYSDATETIME(),
   ngay_phan_hoi DATETIME2 NULL
  );
  CREATE INDEX ix_mau_banh_yeu_cau ON dbo.mau_banh_thiet_ke(banh_rieng_id,id DESC);
 END;
 IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.don_hang') AND name='ix_don_lich_giao')
  CREATE INDEX ix_don_lich_giao ON dbo.don_hang(ngay_giao,khung_gio);
 IF NOT EXISTS(SELECT 1 FROM sys.indexes WHERE object_id=OBJECT_ID('dbo.yeu_cau_banh_rieng') AND name='ix_banh_rieng_lich_nhan')
  CREATE INDEX ix_banh_rieng_lich_nhan ON dbo.yeu_cau_banh_rieng(ngay_nhan,khung_gio);
 -- Một thông báo tổng hợp cho mỗi admin về kho thấp đã có trước đợt nâng cấp.
 IF EXISTS(SELECT 1 FROM dbo.san_pham WHERE dang_ap_dung=1 AND ton_kho<5)
  INSERT INTO dbo.thong_bao(nguoi_dung_id,tieu_de,noi_dung,lien_ket,da_doc,ngay_tao)
  SELECT u.id,N'Kiểm tra tồn kho khi nâng cấp',N'Có bánh sắp hết hoặc đã hết. Bấm để xem danh sách và điều chỉnh tồn kho.',
   '/admin/products?stock=warning',0,SYSDATETIME()
  FROM dbo.nguoi_dung u WHERE u.vai_tro='ADMIN' AND u.duoc_hoat_dong=1
  AND NOT EXISTS(SELECT 1 FROM dbo.thong_bao n WHERE n.nguoi_dung_id=u.id AND n.tieu_de=N'Kiểm tra tồn kho khi nâng cấp');
 COMMIT TRANSACTION;
 PRINT N'Đã bổ sung duyệt mẫu, chỉ mục lịch sản xuất và thông báo tồn kho.';
END TRY
BEGIN CATCH
 IF @@TRANCOUNT>0 ROLLBACK TRANSACTION;
 THROW;
END CATCH;
GO
