/*
 * Copyright 2022-2024 Revetware LLC.
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

package com.soklet.example.util;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.concurrent.NotThreadSafe;
import javax.annotation.concurrent.ThreadSafe;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;

import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

/**
 * @author <a href="https://www.revetkn.com">Mark Allen</a>
 */
@ThreadSafe
public class PasswordManager {
	@Nonnull
	private static final String DEFAULT_RNG_ALGORITHM;
	@Nonnull
	private static final Integer DEFAULT_ITERATIONS;
	@Nonnull
	private static final Integer DEFAULT_SALT_LENGTH;
	@Nonnull
	private static final Integer DEFAULT_KEY_LENGTH;

	static {
		DEFAULT_RNG_ALGORITHM = "SHA1PRNG";
		DEFAULT_ITERATIONS = 210_000; // OWASP 2023 recommendation
		DEFAULT_SALT_LENGTH = 64;
		DEFAULT_KEY_LENGTH = 128 * 16;
	}

	@Nonnull
	private final String hashAlgorithm;
	@Nonnull
	private final String rngAlgorithm;
	@Nonnull
	private final Integer iterations;
	@Nonnull
	private final Integer saltLength;
	@Nonnull
	private final Integer keyLength;

	@Nonnull
	public static Builder withHashAlgorithm(@Nonnull String hashAlgorithm) {
		requireNonNull(hashAlgorithm);
		return new Builder(hashAlgorithm);
	}

	protected PasswordManager(@Nonnull Builder builder) {
		requireNonNull(builder);

		this.hashAlgorithm = requireNonNull(builder.hashAlgorithm);
		this.rngAlgorithm = builder.rngAlgorithm == null ? DEFAULT_RNG_ALGORITHM : builder.rngAlgorithm;
		this.iterations = builder.iterations == null ? DEFAULT_ITERATIONS : builder.iterations;
		this.saltLength = builder.saltLength == null ? DEFAULT_SALT_LENGTH : builder.saltLength;
		this.keyLength = builder.keyLength == null ? DEFAULT_KEY_LENGTH : builder.keyLength;
	}

	@Nonnull
	public String hashPassword(@Nonnull String plaintextPassword) {
		requireNonNull(plaintextPassword);

		try {
			// Generate the salt
			byte[] salt = new byte[getSaltLength()];
			SecureRandom secureRandom = SecureRandom.getInstance(getRngAlgorithm());
			secureRandom.nextBytes(salt);

			// Generate the hash
			PBEKeySpec keySpec = new PBEKeySpec(plaintextPassword.toCharArray(), salt, getIterations(), getKeyLength());
			SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(getHashAlgorithm());
			byte[] hashedPassword = secretKeyFactory.generateSecret(keySpec).getEncoded();

			// Generate a string of the form:
			// <hash algorithm>:<iterations>:<key length>:<salt>:<hashed password>
			return format("%s:%d:%d:%s:%s", getHashAlgorithm(), getIterations(), getKeyLength(), base64Encode(salt), base64Encode(hashedPassword));
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}

	@Nonnull
	public Boolean verifyPassword(@Nonnull String plaintextPassword,
																@Nonnull String hashedPassword) {
		requireNonNull(plaintextPassword);
		requireNonNull(hashedPassword);

		String[] components = hashedPassword.split(":");
		String hashAlgorithm = components[0];
		int iterations = Integer.parseInt(components[1]);
		int keyLength = Integer.parseInt(components[2]);
		byte[] salt = base64Decode(components[3]);
		byte[] hashedPasswordComponent = base64Decode(components[4]);

		try {
			PBEKeySpec keySpec = new PBEKeySpec(plaintextPassword.toCharArray(), salt, iterations, keyLength);
			SecretKeyFactory secretKeyFactory = SecretKeyFactory.getInstance(hashAlgorithm);
			byte[] comparisonHash = secretKeyFactory.generateSecret(keySpec).getEncoded();
			return Arrays.equals(hashedPasswordComponent, comparisonHash);
		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			throw new RuntimeException(e);
		}
	}

	@Nonnull
	protected String base64Encode(@Nonnull byte[] bytes) {
		requireNonNull(bytes);
		return Base64.getEncoder().withoutPadding().encodeToString(bytes);
	}

	@Nonnull
	protected static byte[] base64Decode(@Nonnull String string) {
		requireNonNull(string);
		return Base64.getDecoder().decode(string);
	}

	@NotThreadSafe
	public static class Builder {
		@Nonnull
		private String hashAlgorithm;
		@Nullable
		private String rngAlgorithm;
		@Nullable
		private Integer iterations;
		@Nullable
		private Integer saltLength;
		@Nullable
		private Integer keyLength;

		protected Builder(@Nonnull String hashAlgorithm) {
			requireNonNull(hashAlgorithm);
			this.hashAlgorithm = hashAlgorithm;
		}

		@Nonnull
		public Builder hashAlgorithm(@Nonnull String hashAlgorithm) {
			requireNonNull(hashAlgorithm);
			this.hashAlgorithm = hashAlgorithm;
			return this;
		}

		@Nonnull
		public Builder rngAlgorithm(@Nullable String rngAlgorithm) {
			this.rngAlgorithm = rngAlgorithm;
			return this;
		}

		@Nonnull
		public Builder iterations(@Nullable Integer iterations) {
			this.iterations = iterations;
			return this;
		}

		@Nonnull
		public Builder saltLength(@Nullable Integer saltLength) {
			this.saltLength = saltLength;
			return this;
		}

		@Nonnull
		public Builder keyLength(@Nullable Integer keyLength) {
			this.keyLength = keyLength;
			return this;
		}

		@Nonnull
		public PasswordManager build() {
			return new PasswordManager(this);
		}
	}

	@Nonnull
	public String getHashAlgorithm() {
		return this.hashAlgorithm;
	}

	@Nonnull
	public String getRngAlgorithm() {
		return this.rngAlgorithm;
	}

	@Nonnull
	public Integer getIterations() {
		return this.iterations;
	}

	@Nonnull
	public Integer getSaltLength() {
		return this.saltLength;
	}

	@Nonnull
	public Integer getKeyLength() {
		return this.keyLength;
	}
}