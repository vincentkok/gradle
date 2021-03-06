/*
 * Copyright 2011 the original author or authors.
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
package org.gradle.api.internal.externalresource.transport.http;

import org.gradle.api.artifacts.repositories.PasswordCredentials;
import org.gradle.api.internal.artifacts.repositories.cachemanager.RepositoryArtifactCache;
import org.gradle.api.internal.externalresource.cached.CachedExternalResourceIndex;
import org.gradle.api.internal.externalresource.transfer.DefaultCacheAwareExternalResourceAccessor;
import org.gradle.api.internal.externalresource.transfer.ProgressLoggingExternalResourceAccessor;
import org.gradle.api.internal.externalresource.transfer.ProgressLoggingExternalResourceUploader;
import org.gradle.api.internal.externalresource.transport.AbstractRepositoryTransport;
import org.gradle.api.internal.externalresource.transport.DefaultExternalResourceRepository;
import org.gradle.api.internal.externalresource.transport.ExternalResourceRepository;
import org.gradle.api.internal.file.TemporaryFileProvider;
import org.gradle.logging.ProgressLoggerFactory;
import org.gradle.util.BuildCommencedTimeProvider;

public class HttpTransport extends AbstractRepositoryTransport {
    private final ExternalResourceRepository repository;

    public HttpTransport(String name, PasswordCredentials credentials, RepositoryArtifactCache repositoryCacheManager,
                         ProgressLoggerFactory progressLoggerFactory, TemporaryFileProvider temporaryFileProvider,
                         CachedExternalResourceIndex<String> cachedExternalResourceIndex, BuildCommencedTimeProvider timeProvider) {
        super(name, repositoryCacheManager);
        repository = createRepository(credentials, progressLoggerFactory, temporaryFileProvider, cachedExternalResourceIndex, timeProvider);
    }

    public ExternalResourceRepository getRepository() {
        return repository;
    }

    private ExternalResourceRepository createRepository(PasswordCredentials credentials, ProgressLoggerFactory progressLoggerFactory,
                                                        TemporaryFileProvider temporaryFileProvider, CachedExternalResourceIndex<String> cachedExternalResourceIndex, BuildCommencedTimeProvider timeProvider) {
        HttpClientHelper http = new HttpClientHelper(new DefaultHttpSettings(credentials));
        HttpResourceAccessor accessor = new HttpResourceAccessor(http);
        HttpResourceUploader uploader = new HttpResourceUploader(http);
        ProgressLoggingExternalResourceAccessor loggingAccessor = new ProgressLoggingExternalResourceAccessor(accessor, progressLoggerFactory);
        return new DefaultExternalResourceRepository(
                name,
                accessor,
                new ProgressLoggingExternalResourceUploader(uploader, progressLoggerFactory),
                new HttpResourceLister(accessor),
                temporaryFileProvider,
                new DefaultCacheAwareExternalResourceAccessor(loggingAccessor, cachedExternalResourceIndex, timeProvider)
        );
    }
}
