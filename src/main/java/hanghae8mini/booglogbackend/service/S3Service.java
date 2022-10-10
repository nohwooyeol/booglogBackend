package hanghae8mini.booglogbackend.service;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import hanghae8mini.booglogbackend.domain.Member;
import hanghae8mini.booglogbackend.repository.MemberRepository;
import hanghae8mini.booglogbackend.shared.CommonUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3Client amazonS3Client;
    private final MemberRepository memberRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    private final String cloudFrontDomain = "https://d1ig9s8koiuspp.cloudfront.net/";

    @Transactional
    public String uploadFile(MultipartFile multipartFile) throws IOException {
        String imgUrl = null;

        if (!multipartFile.isEmpty()) {
            String fileName = CommonUtils.buildFileName(multipartFile.getOriginalFilename());
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentType(multipartFile.getContentType());
            amazonS3Client.putObject(new PutObjectRequest(bucketName, fileName, multipartFile.getInputStream(), objectMetadata)
                    .withCannedAcl(CannedAccessControlList.PublicRead));
            imgUrl = amazonS3Client.getUrl(bucketName, fileName).toString();

        }

        String[] nameWithNoS3info = imgUrl.split(".com/");
        String imgName = nameWithNoS3info[1];

       Member member = Member.builder()
               .imageUrl(imgName)
               .build();
        memberRepository.save(member);

        String result = cloudFrontDomain + imgName;
        return result;
    }

}
