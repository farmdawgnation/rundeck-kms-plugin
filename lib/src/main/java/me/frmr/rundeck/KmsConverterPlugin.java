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
import java.io.*;

@Plugin(name="KmsConverterPlugin", service=ServiceNameConstants.StorageConverter)
@PluginDescription(title="Rundeck KMS Plugin", description="Encrypts storage data using Amazon KMS")
public class KmsConverterPlugin implements StorageConverterPlugin {
  /**
   * The ARN to the Key Management Service Key to access.
   */
  @PluginProperty(title="Key ARN", description="The ARN to the key to use", required=true)
  String keyArn;

  /** read the stored data, decrypt if necessary */
  public HasInputStream readResource(
      Path path,
      ResourceMetaBuilder resourceMetaBuilder,
      HasInputStream hasInputStream){
    var crypto = AwsCrypto.builder()
      .withCommitmentPolicy(CommitmentPolicy.RequireEncryptRequireDecrypt)
      .build();
      
    var keyProvider = KmsMasterKeyProvider.builder().buildStrict(keyArn);
    var materialsManager = new DefaultCryptoMaterialsManager(keyProvider);
    
    return new HasInputStream() {
      @Override
      public InputStream getInputStream() throws IOException {
        return crypto.createDecryptingStream(materialsManager, hasInputStream.getInputStream());
      }
      
      @Override
      public long writeContent(OutputStream outputStream) throws IOException {
        return hasInputStream.writeContent(outputStream);
      }
    };
  }

  /** encrypt data to be stored if necessary */
  public HasInputStream createResource(
      Path path,
      ResourceMetaBuilder resourceMetaBuilder,
      HasInputStream hasInputStream){
    var crypto = AwsCrypto.builder()
      .withCommitmentPolicy(CommitmentPolicy.RequireEncryptRequireDecrypt)
      .build();
      
    var keyProvider = KmsMasterKeyProvider.builder().buildStrict(keyArn);
    var encryptionContext = Collections.singletonMap("path", path.toString());
    var materialsManager = new DefaultCryptoMaterialsManager(keyProvider);
    
    return new HasInputStream() {
      @Override
      public InputStream getInputStream() throws IOException {
        return crypto.createEncryptingStream(materialsManager, hasInputStream.getInputStream(), encryptionContext);
      }
      
      @Override
      public long writeContent(OutputStream outputStream) throws IOException {
        return hasInputStream.writeContent(outputStream);
      }
    };
  }

  /** encrypt data to be stored if necessary */
  public HasInputStream updateResource(
      Path path,
      ResourceMetaBuilder resourceMetaBuilder,
      HasInputStream hasInputStream){
    // Same implementation
    return createResource(path, resourceMetaBuilder, hasInputStream);
  }
}