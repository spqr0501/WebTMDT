package com.example.demo.service;

import com.example.demo.dto.ReviewForm;
import com.example.demo.entity.*;
import com.example.demo.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.*;

@Service
public class ReviewService {
    public record RatingSummary(long count, double average, Map<Integer,Long> counts) {}
    private final ReviewRepository reviews;
    private final OrderItemRepository items;
    private final UserRepository users;
    private final AccountService accounts;
    private final CatalogService catalog;
    private final MediaService media;

    public ReviewService(ReviewRepository reviews, OrderItemRepository items, UserRepository users,
                         AccountService accounts, CatalogService catalog, MediaService media) {
        this.reviews=reviews; this.items=items; this.users=users;
        this.accounts=accounts; this.catalog=catalog; this.media=media;
    }

    public boolean canReview(Long productId, String email) {
        if(email==null)return false;
        Long userId=accounts.current(email).getId();
        return items.completedPurchases(userId,productId)>0 && !reviews.existsByProductIdAndUserId(productId,userId);
    }

    public RatingSummary summary(Long productId) {
        Map<Integer,Long> counts=new LinkedHashMap<>();
        for(int star=5;star>=1;star--) counts.put(star,0L);
        long count=0,total=0;
        for(Object[] row:reviews.distribution(productId)) {
            int star=((Number)row[0]).intValue(); long n=((Number)row[1]).longValue();
            counts.put(star,n); count+=n; total+=star*n;
        }
        return new RatingSummary(count,count==0?0:(double)total/count,counts);
    }

    @Transactional(isolation=org.springframework.transaction.annotation.Isolation.READ_COMMITTED)
    public void submit(String email, Long productId, ReviewForm form, MultipartFile[] photos) {
        User user=users.lockById(accounts.current(email).getId()).orElseThrow();
        if(!user.getEnabled())throw new BusinessException("Tài khoản đã bị khóa.");
        if(items.completedPurchases(user.getId(),productId)==0)
            throw new BusinessException("Bạn chỉ có thể đánh giá bánh sau khi đơn hàng đã hoàn thành.");
        if(reviews.existsByProductIdAndUserId(productId,user.getId()))
            throw new BusinessException("Bạn đã đánh giá sản phẩm này. Mỗi khách có một đánh giá cho mỗi sản phẩm.");
        if(form.getRating()==null||form.getRating()<1||form.getRating()>5||form.getComment()==null||form.getComment().strip().length()<10||form.getComment().length()>2000)
            throw new BusinessException("Chọn từ 1 đến 5 sao và viết nhận xét từ 10 đến 2.000 ký tự.");
        List<MultipartFile> files=photos==null?List.of():Arrays.stream(photos).filter(f->!f.isEmpty()).toList();
        if(files.size()>3)throw new BusinessException("Mỗi đánh giá được đính kèm tối đa 3 ảnh.");
        Review review=new Review();review.setUser(user);review.setProduct(catalog.product(productId));
        review.setRating(form.getRating());review.setComment(form.getComment().strip());
        List<String> urls=new ArrayList<>();
        for(MultipartFile file:files) urls.add(media.saveReviewPhoto(file));
        if(urls.size()>0)review.setPhoto1(urls.get(0));
        if(urls.size()>1)review.setPhoto2(urls.get(1));
        if(urls.size()>2)review.setPhoto3(urls.get(2));
        reviews.saveAndFlush(review);
    }

    @Transactional
    public void moderate(Long id, boolean visible, String reason) {
        Review r=reviews.findById(id).orElseThrow(()->new BusinessException("Không tìm thấy đánh giá."));
        if(!visible && (reason==null||reason.isBlank()||reason.length()>300))
            throw new BusinessException("Cần ghi lý do ẩn đánh giá, tối đa 300 ký tự.");
        r.setVisible(visible);r.setModerationReason(visible?null:reason.strip());
    }
}
