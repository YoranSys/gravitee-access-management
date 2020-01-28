/**
 * Copyright (C) 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.am.service;

import io.gravitee.am.common.factor.FactorType;
import io.gravitee.am.model.Application;
import io.gravitee.am.model.Factor;
import io.gravitee.am.model.common.event.Event;
import io.gravitee.am.repository.exceptions.TechnicalException;
import io.gravitee.am.repository.management.api.FactorRepository;
import io.gravitee.am.service.exception.FactorAlreadyExistsException;
import io.gravitee.am.service.exception.FactorNotFoundException;
import io.gravitee.am.service.exception.FactorWithApplicationsException;
import io.gravitee.am.service.exception.TechnicalManagementException;
import io.gravitee.am.service.impl.FactorServiceImpl;
import io.gravitee.am.service.model.NewFactor;
import io.gravitee.am.service.model.UpdateFactor;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import io.reactivex.observers.TestObserver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
@RunWith(MockitoJUnitRunner.class)
public class FactorServiceTest {

    @InjectMocks
    private FactorService factorService = new FactorServiceImpl();

    @Mock
    private EventService eventService;

    @Mock
    private ApplicationService applicationService;

    @Mock
    private FactorRepository factorRepository;

    @Mock
    private AuditService auditService;

    private final static String DOMAIN = "domain1";

    @Test
    public void shouldFindById() {
        when(factorRepository.findById("my-factor")).thenReturn(Maybe.just(new Factor()));
        TestObserver testObserver = factorService.findById("my-factor").test();

        testObserver.awaitTerminalEvent();
        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValueCount(1);
    }

    @Test
    public void shouldFindById_notExistingFactor() {
        when(factorRepository.findById("my-factor")).thenReturn(Maybe.empty());
        TestObserver testObserver = factorService.findById("my-factor").test();
        testObserver.awaitTerminalEvent();

        testObserver.assertNoValues();
    }

    @Test
    public void shouldFindById_technicalException() {
        when(factorRepository.findById("my-factor")).thenReturn(Maybe.error(TechnicalException::new));
        TestObserver testObserver = new TestObserver();
        factorService.findById("my-factor").subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldFindByDomain() {
        when(factorRepository.findByDomain(DOMAIN)).thenReturn(Single.just(Collections.singleton(new Factor())));
        TestObserver<List<Factor>> testObserver = factorService.findByDomain(DOMAIN).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();
        testObserver.assertValue(factors -> factors.size() == 1);
    }

    @Test
    public void shouldFindByDomain_technicalException() {
        when(factorRepository.findByDomain(DOMAIN)).thenReturn(Single.error(TechnicalException::new));

        TestObserver testObserver = new TestObserver<>();
        factorService.findByDomain(DOMAIN).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldCreate() {
        NewFactor newFactor = Mockito.mock(NewFactor.class);
        when(newFactor.getFactorType()).thenReturn(FactorType.TOTP);
        when(factorRepository.findByDomainAndFactorType(DOMAIN, FactorType.TOTP)).thenReturn(Maybe.empty());
        when(factorRepository.create(any(Factor.class))).thenReturn(Single.just(new Factor()));
        when(eventService.create(any())).thenReturn(Single.just(new Event()));

        TestObserver testObserver = factorService.create(DOMAIN, newFactor).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(factorRepository, times(1)).findByDomainAndFactorType(anyString(), anyString());
        verify(factorRepository, times(1)).create(any(Factor.class));
    }

    @Test
    public void shouldCreate_technicalException() {
        NewFactor newFactor = Mockito.mock(NewFactor.class);
        when(newFactor.getFactorType()).thenReturn(FactorType.TOTP);
        when(factorRepository.findByDomainAndFactorType(DOMAIN, FactorType.TOTP)).thenReturn(Maybe.error(TechnicalException::new));

        TestObserver<Factor> testObserver = new TestObserver<>();
        factorService.create(DOMAIN, newFactor).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(factorRepository, never()).create(any(Factor.class));
    }

    @Test
    public void shouldCreate2_technicalException() {
        NewFactor newFactor = Mockito.mock(NewFactor.class);
        when(newFactor.getFactorType()).thenReturn(FactorType.TOTP);
        when(factorRepository.findByDomainAndFactorType(DOMAIN, FactorType.TOTP)).thenReturn(Maybe.empty());
        when(factorRepository.create(any(Factor.class))).thenReturn(Single.error(TechnicalException::new));

        TestObserver<Factor> testObserver = new TestObserver<>();
        factorService.create(DOMAIN, newFactor).subscribe(testObserver);

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(factorRepository, times(1)).findByDomainAndFactorType(anyString(), anyString());
    }

    @Test
    public void shouldCreate_existingFactor() {
        NewFactor newFactor = Mockito.mock(NewFactor.class);
        when(newFactor.getFactorType()).thenReturn(FactorType.TOTP);
        when(factorRepository.findByDomainAndFactorType(DOMAIN, FactorType.TOTP)).thenReturn(Maybe.just(new Factor()));

        TestObserver<Factor> testObserver = new TestObserver<>();
        factorService.create(DOMAIN, newFactor).subscribe(testObserver);

        testObserver.assertError(FactorAlreadyExistsException.class);
        testObserver.assertNotComplete();

        verify(factorRepository, never()).create(any(Factor.class));
    }

    @Test
    public void shouldUpdate() {
        UpdateFactor updateFactor = Mockito.mock(UpdateFactor.class);
        when(updateFactor.getName()).thenReturn("my-factor");
        when(factorRepository.findById("my-factor")).thenReturn(Maybe.just(new Factor()));
        when(factorRepository.update(any(Factor.class))).thenReturn(Single.just(new Factor()));
        when(eventService.create(any())).thenReturn(Single.just(new Event()));

        TestObserver testObserver = factorService.update(DOMAIN, "my-factor", updateFactor).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(factorRepository, times(1)).findById(anyString());
        verify(factorRepository, times(1)).update(any(Factor.class));
    }

    @Test
    public void shouldUpdate_technicalException() {
        UpdateFactor updateFactor = Mockito.mock(UpdateFactor.class);
        when(factorRepository.findById("my-factor")).thenReturn(Maybe.error(TechnicalException::new));

        TestObserver testObserver = factorService.update(DOMAIN, "my-factor", updateFactor).test();
        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(factorRepository, times(1)).findById(anyString());
        verify(factorRepository, never()).update(any(Factor.class));
    }

    @Test
    public void shouldUpdate2_technicalException() {
        UpdateFactor updateFactor = Mockito.mock(UpdateFactor.class);
        when(updateFactor.getName()).thenReturn("my-factor");
        when(factorRepository.findById("my-factor")).thenReturn(Maybe.just(new Factor()));
        when(factorRepository.update(any(Factor.class))).thenReturn(Single.error(TechnicalException::new));

        TestObserver testObserver = factorService.update(DOMAIN, "my-factor", updateFactor).test();
        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();

        verify(factorRepository, times(1)).findById(anyString());
        verify(factorRepository, times(1)).update(any(Factor.class));
    }

    @Test
    public void shouldDelete_notFactor() {
        when(factorRepository.findById("my-factor")).thenReturn(Maybe.empty());

        TestObserver testObserver = factorService.delete(DOMAIN, "my-factor").test();

        testObserver.assertError(FactorNotFoundException.class);
        testObserver.assertNotComplete();

        verify(applicationService, never()).findByFactor(anyString());
        verify(factorRepository, never()).delete(anyString());
    }

    @Test
    public void shouldNotDelete_factorWithClients() {
        Factor factor = new Factor();
        factor.setId("factor-id");
        when(factorRepository.findById(factor.getId())).thenReturn(Maybe.just(factor));
        when(applicationService.findByFactor(factor.getId())).thenReturn(Single.just(Collections.singleton(new Application())));

        TestObserver testObserver = factorService.delete(DOMAIN, factor.getId()).test();

        testObserver.assertError(FactorWithApplicationsException.class);
        testObserver.assertNotComplete();

        verify(factorRepository, never()).delete(anyString());
    }

    @Test
    public void shouldDelete_technicalException() {
        when(factorRepository.findById("my-factor")).thenReturn(Maybe.error(TechnicalException::new));

        TestObserver testObserver = factorService.delete(DOMAIN, "my-factor").test();

        testObserver.assertError(TechnicalManagementException.class);
        testObserver.assertNotComplete();
    }

    @Test
    public void shouldDelete() {
        Factor factor = new Factor();
        factor.setId("factor-id");
        when(factorRepository.findById(factor.getId())).thenReturn(Maybe.just(factor));
        when(applicationService.findByFactor(factor.getId())).thenReturn(Single.just(Collections.emptySet()));
        when(factorRepository.delete(factor.getId())).thenReturn(Completable.complete());
        when(eventService.create(any())).thenReturn(Single.just(new Event()));

        TestObserver testObserver = factorService.delete(DOMAIN, factor.getId()).test();
        testObserver.awaitTerminalEvent();

        testObserver.assertComplete();
        testObserver.assertNoErrors();

        verify(factorRepository, times(1)).delete(factor.getId());
    }
}
