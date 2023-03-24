package io.apt4u.main.converter;


import java.security.MessageDigest;
import java.util.Base64;


public class PasswordCryptConverter{

	private static final String ALGORITHM = "SHA-256";

	public String convertToDatabaseColumn(String attribute) {
		try
		{
			MessageDigest md = MessageDigest.getInstance(ALGORITHM);
			md.update(attribute.getBytes());

			return new String(Base64.getEncoder().encode(md.digest()));
		}
			catch (Exception e)
		{
			throw new RuntimeException(e);
		}
	}

}
