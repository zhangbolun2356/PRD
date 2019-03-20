package pers.bolun.prd.arcore.augmentedimage;

import android.util.Log;
import com.google.ar.core.Config;
import com.google.ar.core.Session;
import com.google.ar.sceneform.ux.ArFragment;

/**
 * Extend the ArFragment to customize the ARCore session configuration to include Augmented Images.
 */
public class CustomArFragment extends ArFragment {

    //First of all, we are setting the plane discovery instruction to null.
    //By doing this, we turn off that hand icon which appears just after the fragment is initialized which instructs the user to move his phone around.
    //We don’t need it any more as we are not detecting random planes but a specific image.
    //Next, we set the update mode for the session to LATEST_CAMERA_IMAGE.
    //This ensures that update listener is called whenever the camera frame updates.
    //It configures the behavior of update method.
    @Override
    protected Config getSessionConfiguration(Session session) {
        getPlaneDiscoveryController().setInstructionView(null);
        Config config = new Config(session);
        config.setUpdateMode(Config.UpdateMode.LATEST_CAMERA_IMAGE);
        session.configure(config);
        getArSceneView().setupSession(session);

        if ((((MainActivity) getActivity()).setupAugmentedImagesDb(config, session))) {
            Log.d("SetupAugImgDb", "Success");
        } else {
            Log.e("SetupAugImgDb","Faliure setting up db");
        }

        return config;
    }
}
