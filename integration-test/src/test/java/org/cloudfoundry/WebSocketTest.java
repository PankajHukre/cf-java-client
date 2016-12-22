/*
 * Copyright 2013-2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cloudfoundry;

import org.cloudfoundry.client.CloudFoundryClient;
import org.cloudfoundry.doppler.DopplerClient;
import org.cloudfoundry.operations.CloudFoundryOperations;
import org.cloudfoundry.operations.DefaultCloudFoundryOperations;
import org.cloudfoundry.operations.applications.LogsRequest;
import org.cloudfoundry.reactor.ConnectionContext;
import org.cloudfoundry.reactor.DefaultConnectionContext;
import org.cloudfoundry.reactor.TokenProvider;
import org.cloudfoundry.reactor.client.ReactorCloudFoundryClient;
import org.cloudfoundry.reactor.doppler.ReactorDopplerClient;
import org.cloudfoundry.reactor.tokenprovider.PasswordGrantTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import reactor.core.Cancellation;

import java.io.IOException;

@Configuration
@EnableAutoConfiguration
public class WebSocketTest {

    public static void main(String[] args) throws InterruptedException, IOException {
        Cancellation cancellation = new SpringApplicationBuilder(WebSocketTest.class)
            .web(false)
            .run(args)
            .getBean(Runner.class)
            .run();

        System.in.read();
        cancellation.dispose();
    }

    @Bean(initMethod = "checkCompatibility")
    ReactorCloudFoundryClient cloudFoundryClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorCloudFoundryClient.builder()
            .connectionContext(connectionContext)
            .tokenProvider(tokenProvider)
            .build();
    }

    @Bean
    DefaultCloudFoundryOperations cloudFoundryOperations(CloudFoundryClient cloudFoundryClient, DopplerClient dopplerClient) {
        return DefaultCloudFoundryOperations.builder()
            .cloudFoundryClient(cloudFoundryClient)
            .dopplerClient(dopplerClient)
            .organization("bhale")
            .space("development")
            .build();
    }

    @Bean
    DefaultConnectionContext connectionContext() {
        return DefaultConnectionContext.builder()
            .apiHost("api.run.pivotal.io")
            .build();
    }


    @Bean
    DopplerClient dopplerClient(ConnectionContext connectionContext, TokenProvider tokenProvider) {
        return ReactorDopplerClient.builder()
            .connectionContext(connectionContext)
            .tokenProvider(tokenProvider)
            .build();
    }

    @Bean
    PasswordGrantTokenProvider tokenProvider() {
        return PasswordGrantTokenProvider.builder()
            .password("rNgdFPs{NgYYVW{PXCAJMqGuokEtVpBgRDu2zjQgJ7nNNFnokA")
            .username("bhale@pivotal.io")
            .build();
    }

    @Component
    private static final class Runner {

        private final CloudFoundryOperations cloudFoundryOperations;

        @Autowired
        private Runner(CloudFoundryOperations cloudFoundryOperations) {
            this.cloudFoundryOperations = cloudFoundryOperations;
        }

        private Cancellation run() {
            return this.cloudFoundryOperations.applications()
                .logs(LogsRequest.builder()
                    .name("java-main-application")
                    .build())
                .subscribe(e -> System.out.print(e.getMessage()), Throwable::printStackTrace);
        }

    }

}
