package flytxt.com.friday

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import ai.api.AIListener;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import android.support.v4.app.AppLaunchChecker
import com.google.gson.JsonElement;
import java.util.Map;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity implements AIListener {

    private Button listenButton;
    private TextView resultTextView;

    private AIService aiService;

}
