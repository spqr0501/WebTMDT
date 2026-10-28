package com.example.demo.service;
import com.example.demo.config.StripeSettings;
import com.example.demo.entity.StripePayment;
import com.stripe.StripeClient;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import com.stripe.net.RequestOptions;
import org.springframework.stereotype.Component;
import java.net.URI;
@Component
public class StripeGateway {
 private final StripeSettings settings;public StripeGateway(StripeSettings settings){this.settings=settings;}
 private StripeClient client(){if(!settings.ready())throw new BusinessException("Stripe Sandbox chưa sẵn sàng. Cần cấu hình khóa thử và bật kết nối webhook.");return new StripeClient(settings.secret());}
 private RequestOptions options(String id){var b=RequestOptions.builder().setConnectTimeout(5000).setReadTimeout(10000).setMaxNetworkRetries(1);if(id!=null)b.setIdempotencyKey(id);return b.build();}
 public static class Rejected extends BusinessException {public Rejected(){super("Stripe từ chối tạo phiên. Kiểm tra khóa thử, số tiền và khả năng thanh toán VND của tài khoản.");}}
 public Session create(StripePayment p){try{
  var params=SessionCreateParams.builder().setMode(SessionCreateParams.Mode.PAYMENT).addAllowedPaymentMethodType(SessionCreateParams.AllowedPaymentMethodType.CARD)
   .setClientReferenceId(p.getId()).putMetadata("attempt_id",p.getId()).setSuccessUrl(settings.baseUrl()+"/payments/stripe/"+p.getId()+"?returned=true")
   .setCancelUrl(settings.baseUrl()+"/payments/stripe/"+p.getId()+"?cancelled=true").setExpiresAt(p.getCreatedAt().plusSeconds(3600).getEpochSecond())
   .setAdaptivePricing(SessionCreateParams.AdaptivePricing.builder().setEnabled(false).build())
   .addLineItem(SessionCreateParams.LineItem.builder().setQuantity(1L).setPriceData(SessionCreateParams.LineItem.PriceData.builder().setCurrency("vnd").setUnitAmount(p.getAmount().longValueExact())
    .setProductData(SessionCreateParams.LineItem.PriceData.ProductData.builder().setName("Bóng Bakery · Thanh toán thử "+(p.getOrder()!=null?"đơn #"+p.getOrder().getId():"bánh riêng #"+p.getCustomCake().getId())).build()).build()).build()).build();
  Session session=client().v1().checkout().sessions().create(params,options(p.getId()));validate(session);return session;
 }catch(BusinessException e){throw e;}catch(com.stripe.exception.InvalidRequestException|com.stripe.exception.AuthenticationException e){throw new Rejected();}catch(Exception e){throw new BusinessException("Chưa tạo được phiên Stripe Sandbox. Hãy thử lại cùng đơn hoặc kiểm tra cấu hình/tính khả dụng của VND trong tài khoản Stripe.");}}
 public Session retrieve(String id){try{Session s=client().v1().checkout().sessions().retrieve(id,options(null));validate(s);return s;}catch(BusinessException e){throw e;}catch(Exception e){throw new BusinessException("Chưa lấy được trạng thái phiên Stripe. Hãy chờ và thử lại.");}}
 public Session expire(String id){try{Session s=client().v1().checkout().sessions().expire(id,options("expire-"+id));validate(s);return s;}catch(Exception e){throw new BusinessException("Chưa đóng được phiên Stripe. Phiên có thể đã thanh toán; hãy chờ xác nhận hoặc thử lại.");}}
 private void validate(Session s){if(s==null||Boolean.TRUE.equals(s.getLivemode())||s.getId()==null||!s.getId().startsWith("cs_test_"))throw new BusinessException("Chỉ cho phép phiên Stripe Sandbox.");if(s.getUrl()!=null){var u=URI.create(s.getUrl());if(!"https".equals(u.getScheme())||!"checkout.stripe.com".equals(u.getHost()))throw new BusinessException("Đường dẫn Stripe không hợp lệ.");}}
}
