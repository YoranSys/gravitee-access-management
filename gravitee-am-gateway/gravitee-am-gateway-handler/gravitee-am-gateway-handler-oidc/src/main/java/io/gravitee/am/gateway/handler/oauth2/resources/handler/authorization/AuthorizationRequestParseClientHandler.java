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
package io.gravitee.am.gateway.handler.oauth2.resources.handler.authorization;

import io.gravitee.am.common.exception.oauth2.InvalidRequestException;
import io.gravitee.am.common.oauth2.Parameters;
import io.gravitee.am.gateway.handler.common.client.ClientSyncService;
import io.gravitee.am.gateway.handler.oauth2.exception.ServerErrorException;
import io.gravitee.am.model.oidc.Client;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.reactivex.ext.web.RoutingContext;

import static io.gravitee.am.common.utils.ConstantKeys.CLIENT_CONTEXT_KEY;


/**
 * The authorization server must ensure that the client used for the Authorization Request is registered and
 * should not redirect to login page if the client does not exist
 *
 * @author Titouan COMPIEGNE (titouan.compiegne at graviteesource.com)
 * @author GraviteeSource Team
 */
public class AuthorizationRequestParseClientHandler implements Handler<RoutingContext> {

    private final ClientSyncService clientSyncService;

    public AuthorizationRequestParseClientHandler(ClientSyncService clientSyncService) {
        this.clientSyncService = clientSyncService;
    }

    @Override
    public void handle(RoutingContext context) {
        final String clientId = context.request().getParam(Parameters.CLIENT_ID);

        fetchClient(clientId, resultHandler -> {
            if (resultHandler.failed()) {
                context.fail(resultHandler.cause());
                return;
            }
            // put client in the execution context
            Client client = resultHandler.result();
            context.put(CLIENT_CONTEXT_KEY, client);

            context.next();
        });
    }

    private void fetchClient(String clientId, Handler<AsyncResult<Client>> authHandler) {
        clientSyncService
                .findByClientId(clientId)
                .subscribe(
                        client -> authHandler.handle(Future.succeededFuture(client)),
                        error -> authHandler.handle(Future.failedFuture(new ServerErrorException("Server error: unable to find client with client_id " + clientId))),
                        () -> authHandler.handle(Future.failedFuture(new InvalidRequestException("No client found for client_id " + clientId)))
                );
    }
}
