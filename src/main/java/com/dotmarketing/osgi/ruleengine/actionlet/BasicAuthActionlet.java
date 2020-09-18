package com.dotmarketing.osgi.ruleengine.actionlet;

import static com.dotcms.repackage.com.google.common.base.Preconditions.checkState;
import java.io.IOException;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang.StringUtils;
import com.dotcms.repackage.com.google.common.base.Preconditions;
import com.dotmarketing.portlets.rules.RuleComponentInstance;
import com.dotmarketing.portlets.rules.actionlet.RuleActionlet;
import com.dotmarketing.portlets.rules.model.ParameterModel;
import com.dotmarketing.portlets.rules.parameter.ParameterDefinition;
import com.dotmarketing.portlets.rules.parameter.display.TextInput;
import com.dotmarketing.portlets.rules.parameter.type.TextType;
import com.dotmarketing.util.Logger;
import io.vavr.control.Try;


public class BasicAuthActionlet extends RuleActionlet<BasicAuthActionlet.Instance> {


    private static final long serialVersionUID = 1L;

    public static final String INPUT_BASICAUTH_KEY = "basicauth";
    private static final String I18N_BASE = "api.system.ruleengine.actionlet.BasicAuth";

    public BasicAuthActionlet() {
        super(I18N_BASE, new ParameterDefinition<>(1, INPUT_BASICAUTH_KEY,
                        new TextInput<>(new TextType().minLength(1))));
    }


    @Override
    public Instance instanceFrom(Map<String, ParameterModel> parameters) {
        return new Instance(parameters);
    }


    @Override
    public boolean evaluate(HttpServletRequest request, HttpServletResponse response, Instance instance) {
        boolean success = false;
        try {


            String auth = Try.of(()->  request.getHeader("Authorization").replace("Basic ", "").trim()).getOrNull();
            if( auth!=null && instance.basicAuth.equals(auth)) {
                return true;
            }
            

            response.setStatus(401);
            response.setHeader("WWW-Authenticate", "Basic");
            Thread.sleep(1000);
            
            response.getWriter().write("401");
            response.getWriter().close();
            return true;




        } catch (Exception e) {
            Logger.error(BasicAuthActionlet.class, "Error executing BasicAuthActionlet.", e);
        }
        return success;
    }


    
    
    

    public class Instance implements RuleComponentInstance {

        String getBasicAuth() {
            return this.basicAuth;
        }
        private final String basicAuth;

        public Instance(Map<String, ParameterModel> parameters) {
            checkState(parameters != null && parameters.size() == 1, "Basic Auth requires parameter '%s'.",
                            INPUT_BASICAUTH_KEY);
            assert parameters != null;
            this.basicAuth = Try.of(()->  Base64.encodeBase64String(parameters.get(INPUT_BASICAUTH_KEY).getValue().getBytes())).getOrNull();
            Preconditions.checkArgument(StringUtils.isNotBlank(this.basicAuth),
                            "BasicAuth requires a user:passwd");

        }
    }
}
