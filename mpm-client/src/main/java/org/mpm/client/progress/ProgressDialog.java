package org.mpm.client.progress;

import com.google.gwt.user.client.Timer;
import com.smartgwt.client.rpc.DMI;
import com.smartgwt.client.util.SC;
import com.smartgwt.client.widgets.Dialog;
import com.smartgwt.client.widgets.Label;
import com.smartgwt.client.widgets.Progressbar;
import java.util.Iterator;
import java.util.Map;

public class ProgressDialog extends Dialog {

    public ProgressDialog(String title, String taskId,
            ProgressCallback progressCallback, String progressTemplate,
            String finishTemplate) {
        super();
        setTitle(title);
        setIsModal(true);
        setShowCloseButton(false);
        setHeight(300);
        setWidth(500);

        Label progressLabel = new Label();
        progressLabel.setWidth100();
        progressLabel.setContents(title);
        addItem(progressLabel);

        Progressbar progressbar = new Progressbar();
        progressbar.setPercentDone(0);
        progressbar.setLength(400);

        addItem(progressbar);

        new Timer() {
            @Override
            public void run() {
                DMI.call("mpm", "org.mpm.server.pics.ProgressDataSource", "getProgress",
                        (rpcResponse, o, rpcRequest) -> {
                            Map result = rpcResponse.getDataAsMap();
                            Integer progress = (Integer) result.get("progress");
                            Iterator iterator = result.keySet().iterator();
                            String progressStr = progressTemplate;
                            String finishedStr = finishTemplate;
                            while (iterator.hasNext()) {
                                Object key = iterator.next();
                                progressStr = progressStr
                                        .replaceAll("\\$" + key + "\\$", "" + result.get(key));
                                finishedStr = finishedStr
                                        .replaceAll("\\$" + key + "\\$", "" + result.get(key));
                            }
                            progressLabel.setContents(progressStr);
                            progressbar.setPercentDone(progress);

                            if (progress < 100) {
                                schedule(500);
                            } else {
                                close();
                                SC.say(finishedStr);
                                progressCallback.callback();
                            }
                        }, new Object[]{taskId});
            }
        }.schedule(500);
    }
}
