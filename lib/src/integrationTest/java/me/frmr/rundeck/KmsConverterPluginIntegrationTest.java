package me.frmr.rundeck;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.dtolabs.rundeck.core.storage.ResourceMetaBuilder;

import org.junit.jupiter.api.Test;
import org.rundeck.storage.api.HasInputStream;
import org.rundeck.storage.api.Path;

class KmsConverterPluginIntegrationTest {
  @Test
  void testEncryptDecrypt() {
    var sut = new KmsConverterPlugin();
    sut.keyArn = System.getenv("KMS_KEY_ARN");

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
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      ByteArrayInputStream bais = new ByteArrayInputStream(new byte[] {});

      @Override
      public InputStream getInputStream() throws IOException {
        return bais;
      }

      @Override
      public long writeContent(OutputStream outputStream) throws IOException {
        baos.writeTo(outputStream);

        return 0;
      }
    };
  }
}
