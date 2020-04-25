/*
 * Copyright 2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.starter.analytics.postgres;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.starter.analytics.Generated;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Controller("/analytics")
@ExecuteOn(TaskExecutors.IO)
public class AnalyticsController {

    private final ApplicationRepository applicationRepository;
    private final FeatureRepository featureRepository;

    public AnalyticsController(
            ApplicationRepository applicationRepository,
            FeatureRepository featureRepository) {
        this.applicationRepository = applicationRepository;
        this.featureRepository = featureRepository;
    }

    @Get("/top/features")
    List<TotalDTO> topFeatures() {
        return featureRepository.topFeatures();
    }


    /**
     * Report analytics.
     * @param generated The generated data
     * @return A future
     */
    @Post("/report")
    @Transactional
    @ExecuteOn(TaskExecutors.IO)
    HttpStatus applicationGenerated(@NonNull @Body Generated generated) {
        Application application = new Application(
                generated.getType(),
                generated.getLanguage(),
                generated.getBuildTool(),
                generated.getTestFramework(),
                generated.getJdkVersion()
        );
        Application saved = applicationRepository.save(application);
        List<Feature> features = generated.getSelectedFeatures().stream()
                .map(f -> new Feature(saved, f.getName()))
                .collect(Collectors.toList());

        featureRepository.saveAll(features);
        return HttpStatus.ACCEPTED;
    }
}
