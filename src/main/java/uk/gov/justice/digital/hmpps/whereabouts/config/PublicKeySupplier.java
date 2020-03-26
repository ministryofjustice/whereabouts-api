package uk.gov.justice.digital.hmpps.whereabouts.config;

import java.security.PublicKey;

public interface PublicKeySupplier {

    PublicKey getPublicKeyForKeyId(String keyId);
}
