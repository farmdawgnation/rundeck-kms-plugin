package me.frmr.rundeck;

import com.dtolabs.rundeck.core.plugins.Plugin;
import com.dtolabs.rundeck.plugins.descriptions.*;
import com.dtolabs.rundeck.core.storage.ResourceMetaBuilder;
import com.dtolabs.rundeck.plugins.ServiceNameConstants;
import com.dtolabs.rundeck.plugins.storage.StorageConverterPlugin;
import org.rundeck.storage.api.HasInputStream;
import org.rundeck.storage.api.Path;
import com.amazonaws.encryptionsdk.*;
import com.amazonaws.encryptionsdk.kms.*;
import java.util.Collections;
import java.util.Map;
import java.io.*;
import com.dtolabs.utils.Streams;

@Plugin(name="rundeck-kms-plugin", service=ServiceNameConstants.StorageConverter)
@PluginDescription(title="Rundeck KMS Plugin", description="Encrypts storage data using Amazon KMS")
public class KmsConverterPlugin implements StorageConverterPlugin {
  public static final String KMS_WAS_ENCRYPTED = "kms-converter-plugin:is-kms-encrypted";

  static boolean wasEncrypted(ResourceMetaBuilder resourceMetaBuilder) {
    return Boolean.parseBoolean(resourceMetaBuilder.getResourceMeta().get(KMS_WAS_ENCRYPTED));
  }

  static void addMetadataWasEncrypted(ResourceMetaBuilder resourceMetaBuilder) {
    resourceMetaBuilder.setMeta(KMS_WAS_ENCRYPTED, Boolean.TRUE.toString());
  }

  /**
   * The ARN to the Key Management Service Key to access.
   */
  @PluginProperty(title="Key ARN", description="The ARN of the KMS key to use for encryption and decryption", required=true)
  String keyArn;

  /** read the stored data, decrypt if necessary */
  public HasInputStream readResource(
      Path path,
      ResourceMetaBuilder resourceMetaBuilder,
      HasInputStream hasInputStream){
    if (! wasEncrypted(resourceMetaBuilder)) {
      // This resource wasn't encrypted with this plugin
      return null;
    }

    KmsMasterKeyProvider keyProvider = KmsMasterKeyProvider.builder().buildStrict(keyArn);
    CryptoMaterialsManager materialsManager = new DefaultCryptoMaterialsManager(keyProvider);
    return new DecryptionStream(hasInputStream, materialsManager);
  }

  /** encrypt data to be stored if necessary */
  public HasInputStream createResource(
      Path path,
      ResourceMetaBuilder resourceMetaBuilder,
      HasInputStream hasInputStream){

    KmsMasterKeyProvider keyProvider = KmsMasterKeyProvider.builder().buildStrict(keyArn);
    Map<String,String> encryptionContext = Collections.singletonMap("path", path.getPath());
    CryptoMaterialsManager materialsManager = new DefaultCryptoMaterialsManager(keyProvider);
    addMetadataWasEncrypted(resourceMetaBuilder);
    return new EncryptionStream(hasInputStream, materialsManager, encryptionContext);
  }

  /** encrypt data to be stored if necessary */
  public HasInputStream updateResource(
      Path path,
      ResourceMetaBuilder resourceMetaBuilder,
      HasInputStream hasInputStream){
    // Same implementation
    return createResource(path, resourceMetaBuilder, hasInputStream);
  }

  /**
   * A KMS encryption stream.
   */
  private static class EncryptionStream implements HasInputStream {
    private final HasInputStream hasInputStream;
    private final CryptoMaterialsManager materialsManager;
    private final Map<String,String> encryptionContext;

    private EncryptionStream(HasInputStream hasInputStream, CryptoMaterialsManager materialsManager, Map<String,String> encryptionContext) {
      this.hasInputStream = hasInputStream;
      this.materialsManager = materialsManager;
      this.encryptionContext = encryptionContext;
    }

    @Override
    public InputStream getInputStream() throws IOException {
      AwsCrypto crypto = AwsCrypto.builder()
        .withCommitmentPolicy(CommitmentPolicy.RequireEncryptRequireDecrypt)
        .build();
      return crypto.createEncryptingStream(materialsManager, hasInputStream.getInputStream(), encryptionContext);
    }

    @Override
    public long writeContent(OutputStream outputStream) throws IOException {
      long bytes = Streams.copyStream(getInputStream(), outputStream);
      return bytes;
    }
  }

  /**
   * A KMS decryption stream
   */
  private static class DecryptionStream implements HasInputStream {
    private final HasInputStream hasInputStream;
    private final CryptoMaterialsManager materialsManager;

    private DecryptionStream(HasInputStream hasInputStream, CryptoMaterialsManager materialsManager) {
      this.hasInputStream = hasInputStream;
      this.materialsManager = materialsManager;
    }

    @Override
    public InputStream getInputStream() throws IOException {
      AwsCrypto crypto = AwsCrypto.builder()
        .withCommitmentPolicy(CommitmentPolicy.RequireEncryptRequireDecrypt)
        .build();
      return crypto.createDecryptingStream(materialsManager, hasInputStream.getInputStream());
    }

    @Override
    public long writeContent(OutputStream outputStream) throws IOException {
      return Streams.copyStream(getInputStream(), outputStream);
    }

  }
}
