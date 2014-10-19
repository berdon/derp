package com.jajuka.derp;

import android.widget.TextView;
import com.jajuka.derp.util.Reflect;
import junit.framework.TestCase;

import java.lang.reflect.Method;

/**
 * Created by austinh on 10/18/14.
 */
public class ReflectTest extends TestCase {
    private static class Derp {
        private String gerp = "qerp";
    }

    private static class Herp {
        private Derp lerp = new Derp();
        private String merp = "berp";
        private String[] ferp = new String[]{"werp", "verp"};
    }

    public void testTranslate() throws Exception {
        Herp herp = new Herp();
        assertEquals(Reflect.translate(herp, "lerp.gerp"), herp.lerp.gerp);
        assertEquals(Reflect.translate(herp, "merp"), herp.merp);
        assertEquals(Reflect.translate(herp, "ferp[0]"), "werp");
    }

    public void testFindMethod() throws Exception {
        FakeTextView textView = new FakeTextView();
        Method method = FakeTextView.class.getMethod("setText", CharSequence.class);
        assertNotNull(method);
        method.invoke(textView, "asdf");

        method = Reflect.findMethod(textView, "setText", String.class);
        assertNotNull(method);
        method.invoke(textView, "asdf");

        assertEquals(2, textView.mValue);
    }

    public class FakeTextView {
        public int mValue = 0;

        public void setText(CharSequence text) {
            mValue++;
        }
    }
}
