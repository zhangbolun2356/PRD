package pers.bolun.prd.arcore.augmentedimage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.ar.core.Anchor;
import com.google.ar.core.AugmentedImage;
import com.google.ar.core.AugmentedImageDatabase;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.sceneform.AnchorNode;
import com.google.ar.sceneform.FrameTime;
import com.google.ar.sceneform.rendering.ModelRenderable;
import com.google.ar.sceneform.rendering.Renderable;
import com.google.ar.sceneform.ux.ArFragment;
import com.google.ar.sceneform.ux.TransformableNode;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

public class MainActivity extends AppCompatActivity {
    ArFragment arFragment;
    boolean shouldAddModel = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        arFragment = (CustomArFragment) getSupportFragmentManager().findFragmentById(R.id.ux_fragment);
        arFragment.getPlaneDiscoveryController().hide();
        arFragment.getArSceneView().getScene().addOnUpdateListener(this::onUpdateFrame);
    }



    //This method is used to build a renderable from the provided Uri.
    //Once the renderable is built it is passed into addNodeToScene method where the renderable is attached to a node and that node is placed onto the scene.
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void placeObject(ArFragment arFragment, Anchor anchor, Uri uri) {
        ModelRenderable.builder()
                .setSource(arFragment.getContext(), uri)
                .build()
                .thenAccept(modelRenderable -> addNodeToScene(arFragment, anchor, modelRenderable))
                .exceptionally(throwable -> {
                            Toast.makeText(arFragment.getContext(), "Error:" + throwable.getMessage(), Toast.LENGTH_LONG).show();
                            return null;
                        }
                );
    }

    //Refresh the frame and place the different 3D objects on different objects
    //Once we have the frame, we analyze for our reference image.
    //We extract a list of all the items ARCore has tracked using frame.getUpdatedTrackables.
    //This is a collection of all the detected images. We then loop over the collection and check if our image is present in the frame.
    //If we find a match, then we go ahead and place a 3D model over the detected image.
    @RequiresApi(api = Build.VERSION_CODES.N)
    private void onUpdateFrame(FrameTime frameTime) {
        Frame frame = arFragment.getArSceneView().getArFrame();

        Collection<AugmentedImage> augmentedImages = frame.getUpdatedTrackables(AugmentedImage.class);
        for (AugmentedImage augmentedImage : augmentedImages) {
            if (augmentedImage.getTrackingState() == TrackingState.TRACKING) {
                if (augmentedImage.getName().equals("woman1") && shouldAddModel) {
                    placeObject(arFragment, augmentedImage.createAnchor(augmentedImage.getCenterPose()), Uri.parse("andy.sfb"));
                    shouldAddModel = true;
                }
                if (augmentedImage.getName().equals("woman2") && shouldAddModel) {
                    placeObject(arFragment, augmentedImage.createAnchor(augmentedImage.getCenterPose()), Uri.parse("woman.sfb"));
                    shouldAddModel = true;
                }
                if (augmentedImage.getName().equals("baby") && shouldAddModel) {
                    placeObject(arFragment, augmentedImage.createAnchor(augmentedImage.getCenterPose()), Uri.parse("andy.sfb"));
                    shouldAddModel = true;
                }
            }
        }
    }


    //Setup new database by code
    // we first initialize our database for this session and then add an image to this database.
    // We will name our image "woman1", "woman2" or "baby". Then we set the database for this session configuration and return true,
    // indicating that the image is added successfully.
    public boolean setupAugmentedImagesDb(Config config, Session session) {
        AugmentedImageDatabase augmentedImageDatabase;
        augmentedImageDatabase = new AugmentedImageDatabase(session);
        String name;
       for(int i = 1; i<23;i++){
            Bitmap bitmap = loadAugmentedImage(i);
            if (bitmap == null) {
                return false;
            }
            if(i<8){
                augmentedImageDatabase.addImage("woman1", bitmap);
            }
            if(i>=8 && i <=16){
                augmentedImageDatabase.addImage("woman2", bitmap);
            }
           if(i > 16){
               augmentedImageDatabase.addImage("baby", bitmap);
           }
        }
        config.setAugmentedImageDatabase(augmentedImageDatabase);
        return true;
    }

    //Load one augmentd Images to application
    private Bitmap loadAugmentedImage(int i) {
        String fileName = i + ".jpg";
        try (InputStream is = getAssets().open(fileName)) {
            return BitmapFactory.decodeStream(is);
        } catch (IOException e) {
            Log.e("ImageLoad", "IO Exception", e);
        }
        return null;
    }

    //This method creates an AnchorNode from the received anchor,
    //creates another node on which the renderable is attached,
    //then adds this node to the AnchorNode and adds the AnchorNode to the scene.
    private void addNodeToScene(ArFragment arFragment, Anchor anchor, Renderable renderable) {
        AnchorNode anchorNode = new AnchorNode(anchor);
        TransformableNode node = new TransformableNode(arFragment.getTransformationSystem());
        node.setRenderable(renderable);
        node.setParent(anchorNode);
        arFragment.getArSceneView().getScene().addChild(anchorNode);
        node.select();
    }


}
