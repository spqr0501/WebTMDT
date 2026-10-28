package com.example.demo.controller;
import com.example.demo.service.BusinessException;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;
@RestControllerAdvice(assignableTypes={MapApiController.class,DeliveryApiController.class})
@org.springframework.core.annotation.Order(org.springframework.core.Ordered.HIGHEST_PRECEDENCE)
public class DeliveryApiAdvice {
 @ExceptionHandler(BusinessException.class) public ResponseEntity<?> business(BusinessException e){return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(java.util.Map.of("message",e.getMessage()));}
 @ExceptionHandler({org.springframework.http.converter.HttpMessageNotReadableException.class,org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class}) public ResponseEntity<?> invalid(Exception e){return ResponseEntity.badRequest().cacheControl(CacheControl.noStore()).body(java.util.Map.of("message","Dữ liệu vị trí chưa hợp lệ."));}
}
