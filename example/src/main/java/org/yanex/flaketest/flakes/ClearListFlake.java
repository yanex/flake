package org.yanex.flaketest.flakes;

import android.view.View;
import android.widget.Button;

import org.jetbrains.annotations.NotNull;
import org.yanex.flake.AbstractFlakeHolder;
import org.yanex.flake.FlakeHolder;
import org.yanex.flake.FlakeManager;
import org.yanex.flake.XmlFlake;
import org.yanex.flaketest.R;

public class ClearListFlake extends XmlFlake<ClearListFlake.Holder> {

    @Override
    public int getLayoutResource() {
        return R.layout.flake_clear_list;
    }

    @NotNull
    @Override
    protected Holder createHolder(@NotNull FlakeManager manager, @NotNull View root) {
        return new Holder(root);
    }

    @Override
    public void init(@NotNull Holder h, @NotNull final FlakeManager manager) {
        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.goBack(v.getId() == R.id.ok ? ClearListMessage.INSTANCE : null);
            }
        };

        h.ok.setOnClickListener(listener);
        h.cancel.setOnClickListener(listener);
    }

    class Holder extends AbstractFlakeHolder {
        public final Button ok;
        public final Button cancel;

        public Holder(@NotNull View root) {
            super(root);

            ok = (Button) root.findViewById(R.id.ok);
            cancel = (Button) root.findViewById(R.id.cancel);
        }
    }

}
