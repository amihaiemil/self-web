/**
 * Copyright (c) 2020, Self XDSD Contributors
 * All rights reserved.
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"),
 * to read the Software only. Permission is hereby NOT GRANTED to use, copy,
 * modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY,
 * OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT
 * OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.selfxdsd.selfweb.api;

import com.selfxdsd.api.Project;
import com.selfxdsd.api.Self;
import com.selfxdsd.api.User;
import com.selfxdsd.api.exceptions.WalletAlreadyExistsException;
import com.selfxdsd.selfweb.api.output.JsonWallet;
import com.selfxdsd.selfweb.api.output.JsonWallets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Project Wallets API.
 * @author Mihai Andronache (amihaiemil@gmail.com)
 * @version $Id$
 * @since 0.0.1
 * @todo #135:30min Add an endpoint and front-end code for a
 *  Wallet's activation.
 * @todo #135:60min Once the PaymentMethod logic is implemented in the core,
 *  we should add a form in the Real Wallet widget, where the User will add
 *  payment methods.
 * @todo #135:60min Once the Wallet.updateCash(...) method is implemented in
 *  the core, we should allow the User to update the Limit of their real
 *  wallet.
 */
@RestController
public class WalletsApi extends BaseApiController {

    /**
     * Logger.
     */
    private static final Logger LOG = LoggerFactory.getLogger(
        WalletsApi.class
    );

    /**
     * Authenticated user.
     */
    private final User user;

    /**
     * Self's core.
     */
    private final Self self;

    /**
     * Ctor.
     * @param user Authenticated user.
     * @param self Self's core.
     */
    @Autowired
    public WalletsApi(final User user, final Self self) {
        this.user = user;
        this.self = self;
    }

    /**
     * Get the wallets of one of the authenticated User's projects.
     * @param owner Owner of the repo.
     * @param name Name of the repo.
     * @return Json wallets.
     */
    @GetMapping(
        value = "/projects/{owner}/{name}/wallets",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> wallets(
        @PathVariable final String owner,
        @PathVariable final String name
    ) {
        final Project found = this.self.projects().getProjectById(
            owner + "/" + name, user.provider().name()
        );
        ResponseEntity<String> response = ResponseEntity.noContent().build();
        if(found != null) {
            response = ResponseEntity.ok(
                new JsonWallets(found.wallets()).toString()
            );
        }
        return response;
    }

    /**
     * Create a Stripe Wallet for one of the authenticated user's projects.
     * @param owner Owner of the project (login of user or org name).
     * @param name Repo name.
     * @return Stripe Wallet as JSON string.
     */
    @PostMapping(
        value = "/projects/{owner}/{name}/wallets/stripe",
        produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<String> createStripeWallet(
        @PathVariable final String owner,
        @PathVariable final String name
    ) {
        ResponseEntity<String> response;
        final Project found = this.self.projects().getProjectById(
            owner + "/" + name, user.provider().name()
        );
        if(found == null) {
            response = ResponseEntity.badRequest().build();
        } else {
            try {
                response = ResponseEntity.ok(
                    new JsonWallet(
                        found.createStripeWallet()
                    ).toString()
                );
            } catch (final WalletAlreadyExistsException ex) {
                LOG.error(
                    "WalletAlreadyExistsException when creating "
                    + "a new Stripe wallet!",
                    ex.getMessage()
                );
                response = ResponseEntity.badRequest().body(
                    ex.toString()
                );
            }
        }
        return response;
    }
}