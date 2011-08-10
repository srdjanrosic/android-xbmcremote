
package org.xbmc.android.remote;

import org.aspectj.lang.Signature;
import android.util.Log;

public aspect Trace {

    pointcut traceMethods() :  (execution(* *(..))&& !cflow(within(Trace)));

    before(): traceMethods() {

        Signature sig = thisJoinPointStaticPart.getSignature();
        String line = "" + thisJoinPointStaticPart.getSourceLocation().getLine();
        String sourceName = thisJoinPointStaticPart.getSourceLocation().getWithinType().getCanonicalName();
        Log.i("AspectJ", "Call from " + sourceName +
                         " line " + line +
                         " to " + sig.getDeclaringTypeName() + "." + sig.getName());

    }

}
