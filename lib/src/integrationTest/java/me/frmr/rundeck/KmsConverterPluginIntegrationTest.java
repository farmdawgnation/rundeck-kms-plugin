package me.frmr.rundeck;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import com.dtolabs.rundeck.core.storage.ResourceMetaBuilder;

import org.junit.jupiter.api.Test;
import org.rundeck.storage.api.HasInputStream;
import org.rundeck.storage.api.Path;
import com.dtolabs.utils.Streams;

public class KmsConverterPluginIntegrationTest {
  @Test
  void testIgnoresNonKmsData() throws IOException {
    var exampleMessage = "Hello, KMS!";
    var sut = new KmsConverterPlugin();
    sut.keyArn = System.getenv("KMS_KEY_ARN");
    sut.accessKeyId = System.getenv("AWS_ACCESS_KEY_ID");
    sut.secretAccessKey = System.getenv("AWS_SECRET_ACCESS_KEY");

    if (sut.keyArn == null) {
      throw new RuntimeException("Env var KMS_KEY_ARN required for integration tests");
    }

    var examplePath = new Path() {
      @Override
      public String getPath() {
        return "/tests/integration/foo";
      }

      @Override
      public String getName() {
        return "foo";
      }
    };
    var exampleMetaBuilder = new ResourceMetaBuilder();
    var exampleInputStream = new HasInputStream() {
      ByteArrayInputStream bais = new ByteArrayInputStream(exampleMessage.getBytes(Charset.defaultCharset()));

      @Override
      public InputStream getInputStream() throws IOException {
        return bais;
      }

      @Override
      public long writeContent(OutputStream outputStream) throws IOException {
        return Streams.copyStream(getInputStream(), outputStream);
      }
    };

    var decryptedStream = sut.readResource(examplePath, exampleMetaBuilder, exampleInputStream);
    assertNull(decryptedStream, "KMS pluting attempted to decrypt something it didn't encrypt");
  }

  @Test
  void testEncryptDecrypt() throws IOException {
    var exampleMessage = "Hello, KMS!";
    var sut = new KmsConverterPlugin();
    sut.keyArn = System.getenv("KMS_KEY_ARN");
    sut.accessKeyId = System.getenv("AWS_ACCESS_KEY_ID");
    sut.secretAccessKey = System.getenv("AWS_SECRET_ACCESS_KEY");

    if (sut.keyArn == null) {
      throw new RuntimeException("Env var KMS_KEY_ARN required for integration tests");
    }

    var examplePath = new Path() {
      @Override
      public String getPath() {
        return "/tests/integration/foo";
      }

      @Override
      public String getName() {
        return "foo";
      }
    };
    var exampleMetaBuilder = new ResourceMetaBuilder();
    var exampleInputStream = new HasInputStream() {
      ByteArrayInputStream bais = new ByteArrayInputStream(exampleMessage.getBytes(Charset.defaultCharset()));

      @Override
      public InputStream getInputStream() throws IOException {
        return bais;
      }

      @Override
      public long writeContent(OutputStream outputStream) throws IOException {
        return Streams.copyStream(getInputStream(), outputStream);
      }
    };

    var encryptedStream = sut.createResource(examplePath, exampleMetaBuilder, exampleInputStream);
    var decryptedStream = sut.readResource(examplePath, exampleMetaBuilder, encryptedStream);

    var decryptedMessage = new String(new BufferedInputStream(decryptedStream.getInputStream()).readAllBytes());

    assertEquals(exampleMessage, decryptedMessage, "Decrypted text did not match input text");
  }
}
