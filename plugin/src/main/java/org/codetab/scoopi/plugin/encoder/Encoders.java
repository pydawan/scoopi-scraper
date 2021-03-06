package org.codetab.scoopi.plugin.encoder;

import static java.util.Objects.nonNull;
import static org.codetab.scoopi.util.Util.dashit;
import static org.codetab.scoopi.util.Util.spaceit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.inject.Inject;

import org.codetab.scoopi.defs.IPluginDef;
import org.codetab.scoopi.exception.DefNotFoundException;
import org.codetab.scoopi.exception.StepRunException;
import org.codetab.scoopi.log.ErrorLogger;
import org.codetab.scoopi.log.Log.CAT;
import org.codetab.scoopi.model.Plugin;

public class Encoders extends HashMap<String, List<IEncoder<?>>> {

    private static final long serialVersionUID = 1L;

    @Inject
    private IPluginDef pluginDef;
    @Inject
    private EncoderFactory encoderFactory;
    @Inject
    private ErrorLogger errorLogger;

    public void createEncoders(final List<Plugin> plugins,
            final String stepsName, final String stepName) {
        for (Plugin plugin : plugins) {
            String appenderName = dashit(stepName, plugin.getName());
            Optional<List<Plugin>> encoderPlugins = null;
            try {
                encoderPlugins = pluginDef.getPlugins(plugin);
            } catch (Exception e) {
                throw new StepRunException("unable to create appenders", e);
            }
            if (nonNull(encoderPlugins) && encoderPlugins.isPresent()) {
                List<IEncoder<?>> encoders = new ArrayList<>();
                for (Plugin encoderPlugin : encoderPlugins.get()) {
                    try {
                        IEncoder<?> encoder =
                                encoderFactory.createEncoder(encoderPlugin);
                        encoders.add(encoder);
                    } catch (ClassCastException | IllegalStateException
                            | ClassNotFoundException | DefNotFoundException e) {
                        String message = spaceit(
                                "unable to create appender from plugin:",
                                plugin.toString());
                        errorLogger.log(CAT.ERROR, message, e);
                    }
                }
                put(appenderName, encoders);
            }
        }
    }
}
