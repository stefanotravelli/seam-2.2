/*
 * JBoss, Home of Professional Open Source
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.seam.wiki.core.ui;

import static org.jboss.seam.ScopeType.APPLICATION;
import org.jboss.seam.annotations.Name;
import org.jboss.seam.annotations.Scope;
import org.jboss.seam.annotations.intercept.BypassInterceptors;
import org.jboss.seam.core.Expressions;
import org.jboss.seam.log.Log;
import org.jboss.seam.log.Logging;
import org.jboss.seam.servlet.ContextualHttpServletRequest;
import org.jboss.seam.util.Resources;
import org.jboss.seam.web.ConditionalAbstractResource;
import org.jboss.seam.wiki.core.plugin.PluginRegistry;
import org.jboss.seam.wiki.core.plugin.metamodel.Plugin;
import org.jboss.seam.wiki.core.plugin.metamodel.PluginModule;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Serves files from the classpath, from a Plugin theme package.
 * <p>
 * This means that any web request can get any file out of that package. So don't put
 * anything into a plugins theme directory that you don't want people to GET.
 * </p>
 * <p>
 * It is primarily used for serving up plugin CSS and image files. It can also interpolate
 * EL expressions in certain resources, configured with <tt>interpolatedResourcesExtensions</tt>.
 * The default is to parse resources with <tt>css</tt> extension. A <tt>PluginModule</tt> instance
 * is always available as variable <tt>currentPluginModule</tt>, which allows direct access to metadata
 * and path information such as <tt>imagePath</tt> and <tt>styleSheetPath</tt>.
 * </p>
 *
 * @author Christian Bauer
 */
@Scope(APPLICATION)
@Name("wikiPluginThemeResource")
@BypassInterceptors
public class WikiPluginThemeResource extends ConditionalAbstractResource {

    private Log log = Logging.getLog(WikiPluginThemeResource.class);

    private static final Pattern EL_PATTERN = Pattern.compile("#" + Pattern.quote("{") + "(.*)" + Pattern.quote("}"));

    // Resources URIs end with /<pluginKey/<pluginModuleKey>/<themeResourceName>.<themeResourceExtension>
    public static Pattern PLUGIN_RESOURCE_PATTERN =
            Pattern.compile("^/(" + Plugin.KEY_PATTERN + ")/(" + Plugin.KEY_PATTERN + ")/(.+?)\\.([a-z]+)$");

    // Resources that are interpolated, i.e. which are text files that contain EL expressions
    private String[] interpolatedResourcesExtensions = new String[]{"css"};

    private Map<String, String> mimeTypesExtensions = new HashMap() {{
        put("css", "text/css");
        put("txt", "text/plain");
        put("js", "text/javascript");
        put("png", "image/png");
        put("jpg", "image/jpg");
        put("jpeg", "image/jpeg");
        put("gif", "image/gif");
        put("swf", "application/x-shockwave-flash");
        put("flv", "video/x-flv");
    }};

    @Override
    public String getResourcePath() {
        return Plugin.REGISTER_SEAM_RESOURCE_THEME;
    }

    @Override
    public void getResource(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException {

        // Wrap this, we need an ApplicationContext
        new ContextualHttpServletRequest(request) {
            @Override
            public void process() throws IOException {
                doWork(request, response);
            }
        }.run();

    }

    public String[] getInterpolatedResourcesExtensions() {
        return interpolatedResourcesExtensions;
    }

    public void setInterpolatedResourcesExtensions(String[] interpolatedResourcesExtensions) {
        this.interpolatedResourcesExtensions = interpolatedResourcesExtensions;
    }

    public void doWork(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        String pathInfo = request.getPathInfo().substring(getResourcePath().length());

        String pluginKey = null;
        String pluginModuleKey = null;
        String themeResourceName = null;
        String themeResourceExtension = null;

        Matcher matcher = PLUGIN_RESOURCE_PATTERN.matcher(pathInfo);
        if (matcher.find()) {
            pluginKey = matcher.group(1);
            pluginModuleKey = matcher.group(2);
            themeResourceName = matcher.group(3);
            themeResourceExtension = matcher.group(4);
            log.debug("request for resource,"
                    + " plugin key '" + pluginKey + "'"
                    + " plugin module key '" + pluginModuleKey + "'"
                    + " theme resource name '" + themeResourceName + "'"
                    + " theme resource ext '" + themeResourceExtension + "'"
            );
        }
        if (pluginKey == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Plugin key not found");
            return;
        }
        if (pluginModuleKey == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Plugin module key not found");
            return;
        }
        if (themeResourceName == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Resource name not found");
            return;
        }

        PluginRegistry registry = PluginRegistry.instance();
        Plugin plugin = registry.getPlugin(pluginKey);
        if (plugin == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Plugin not found in registry: " + pluginKey);
            return;
        }
        PluginModule pluginModule = plugin.getModuleByKey(pluginModuleKey);
        if (pluginModule == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Plugin module not found: " + pluginKey+"."+pluginModuleKey);
            return;
        }

        org.jboss.seam.contexts.Contexts.getEventContext().set("currentPluginModule", pluginModule);

        String resourcePath = plugin.getPackageThemePath() + "/" + themeResourceName + "." + themeResourceExtension;
        InputStream in = Resources.getResourceAsStream(resourcePath, getServletContext());

        if (in != null) {

            boolean isInterpolated = false;
            for (String interpolatedResourcesExtension : interpolatedResourcesExtensions) {
                if (interpolatedResourcesExtension.equals(themeResourceExtension)) {
                    isInterpolated = true;
                    break;
                }
            }

            String contentType = mimeTypesExtensions.get(themeResourceExtension);

            if (isInterpolated) {

                if (contentType == null) {
                    contentType = "text/plain";
                }
                response.setContentType(contentType);

                CharSequence textFile = readFile(in);
                textFile = parseEL(textFile);

                String entityTag = createEntityTag(textFile.toString(), false);
                Long lastModified = getLastModifiedTimestamp(resourcePath);

                if (!sendConditional(request, response, entityTag, lastModified)) {
                    log.debug("serving interpolated resource: " + resourcePath);
                    OutputStreamWriter outStream = new OutputStreamWriter(selectOutputStream(request, response));
                    outStream.write(textFile.toString());
                    outStream.close();
                }
            } else {
                log.debug("serving resource: " + resourcePath);

                if (contentType == null) {
                    contentType = "application/octet-stream";
                }
                response.setContentType(contentType);

                // We can't produce an entity tag because we stream the bytes through (or not, through the gzip wrapper)
                if (!sendConditional(request, response, null, getLastModifiedTimestamp(resourcePath))) {
                    OutputStream outStream = selectOutputStream(request, response);
                    byte[] buffer = new byte[1024];
                    int read = in.read(buffer);
                    while (read != -1) {
                        outStream.write(buffer, 0, read);
                        read = in.read(buffer);
                    }
                    outStream.close();
                }
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, resourcePath);
        }

    }

    private CharSequence readFile(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder css = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            css.append(line);
            css.append("\n");
        }
        inputStream.close();
        return css;
    }

    // Resolve any EL value binding expression present in text resource
    // This should be Interpolator.interpolate, but it seems to break on CSS
    private CharSequence parseEL(CharSequence string) {
        StringBuffer parsed = new StringBuffer(string.length());
        Matcher matcher =
                EL_PATTERN.matcher(string);

        while (matcher.find()) {
            String result = Expressions.instance().createValueExpression(
                "#{" + matcher.group(1) + "}", String.class
            ).getValue();
            if (result != null) {
                matcher.appendReplacement(parsed, result);
            } else {
                matcher.appendReplacement(parsed, "");
            }
        }
        matcher.appendTail(parsed);
        return parsed;
    }

}
