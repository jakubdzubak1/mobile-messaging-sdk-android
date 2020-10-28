package org.infobip.mobile.messaging.demo;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import org.infobip.mobile.messaging.MobileMessaging;
import org.infobip.mobile.messaging.chat.InAppChat;
import org.infobip.mobile.messaging.logging.MobileMessagingLogger;

import java.util.Date;
import java.util.UUID;

/**
 * @author sslavin
 * @since 13/11/2017.
 */

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InAppChat.getInstance(this).activate();

        String token = generateAuthToken("subject", "widgetId", "widgetKeyId", "widgetKeySecret");
        InAppChat.getInstance(this).auth(token);

        setContentView(R.layout.activity_main);
        setSupportActionBar(this.<Toolbar>findViewById(R.id.toolbar));

        Button btn = findViewById(R.id.btn_open);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                InAppChat.getInstance(MainActivity.this).inAppChatView().show();

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_id) {
            copyRegistrationIdToClipboard();
            Toast.makeText(this, R.string.toast_registration_id_copy, Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("ConstantConditions")
    private void copyRegistrationIdToClipboard() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Registration ID", MobileMessaging.getInstance(this).getInstallation().getPushRegistrationId());
        clipboard.setPrimaryClip(clip);
    }

    private String generateAuthToken(String subject, String widgetId, String widgetKeyId, String widgetKeySecret) {
        if (subject != null && widgetKeyId != null && widgetKeySecret != null && widgetId != null) {
            try {
                MACSigner personalizationTokenSigner =new MACSigner(Base64.decode(widgetKeySecret, Base64.DEFAULT));
                String uuid = UUID.randomUUID().toString();
                String log = String.format("GET AUTH TOKEN <<< subject: %s, widgetKeyId: %s, widgetId: %s, uuid: %s", subject, widgetKeyId, widgetId, uuid);
                MobileMessagingLogger.d(log);

                JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                        .jwtID(uuid)
                        .subject(subject)
                        .issuer(widgetId)
                        .issueTime(new Date())
                        .expirationTime(new Date(System.currentTimeMillis() + 15000))
                        .claim("ski", widgetKeyId)
                        .claim("stp", "externalPersonId")
                        .claim("sid", uuid)
                        .build();

                SignedJWT personalizedToken = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claimsSet);
                personalizedToken.sign(personalizationTokenSigner);
                String token = personalizedToken.serialize();
                MobileMessagingLogger.d("SIGNED TOKEN <<< " + token);
                return token;
            } catch (JOSEException e) {
                e.printStackTrace();
            }
        }
        MobileMessagingLogger.d("SIGNED TOKEN " + null);
        return null;
    }

}
