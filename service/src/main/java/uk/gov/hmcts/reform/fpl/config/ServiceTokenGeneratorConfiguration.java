package uk.gov.hmcts.reform.fpl.config;

import com.warrenstrange.googleauth.GoogleAuthenticator;
import feign.Feign;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.web.client.RestTemplate;
import uk.gov.hmcts.reform.authorisation.ServiceAuthorisationApi;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGenerator;
import uk.gov.hmcts.reform.authorisation.generators.AuthTokenGeneratorFactory;

import java.util.Map;

@Configuration
@Lazy
public class ServiceTokenGeneratorConfiguration {

    @Bean
    public AuthTokenGenerator serviceAuthTokenGenerator(
        @Value("${idam.s2s-auth.totp_secret}") String secret,
        @Value("${idam.s2s-auth.microservice}") String microService,
        ServiceAuthorisationApi serviceAuthorisationApi
    ) {
        return AuthTokenGeneratorFactory.createDefaultGenerator(secret, microService, serviceAuthorisationApi);
    }

    public static void main(String[] args) {


        String generate = new ServiceTokenGeneratorConfiguration().serviceAuthTokenGenerator("AABBCCDDEEFFGGHH", "fpl_case_service", new ServiceAuthorisationApi() {
            @Override
            public String serviceToken(Map<String, String> signIn) {
                return new RestTemplate().postForEntity("http://localhost:4502/lease", signIn, String.class).getBody();
            }

            @Override
            public void authorise(String authHeader, String[] roles) {

            }

            @Override
            public String getServiceName(String authHeader) {
                return null;
            }
        }).generate();

        System.out.println(generate);
    }
}
