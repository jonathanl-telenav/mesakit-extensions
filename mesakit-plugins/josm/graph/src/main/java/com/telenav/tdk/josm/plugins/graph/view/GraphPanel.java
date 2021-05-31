////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
//
// © 2011-2021 Telenav, Inc.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//
////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

package com.telenav.kivakit.josm.plugins.graph.view;

import com.telenav.kivakit.graph.Vertex;
import com.telenav.kivakit.josm.plugins.graph.GraphPlugin;
import com.telenav.kivakit.josm.plugins.graph.view.tabs.query.QueryPanel;
import com.telenav.kivakit.josm.plugins.graph.view.tabs.routing.RoutingPanel;
import com.telenav.kivakit.josm.plugins.graph.view.tabs.search.SearchPanel;
import com.telenav.kivakit.josm.plugins.graph.view.tabs.tags.TagPanel;
import com.telenav.kivakit.josm.plugins.graph.view.tabs.view.ViewPanel;
import com.telenav.kivakit.josm.plugins.library.BaseJosmPanel;
import com.telenav.kivakit.kernel.project.KivaKit;
import com.telenav.kivakit.kernel.time.Duration;
import com.telenav.kivakit.utilities.ui.swing.component.Components;
import com.telenav.kivakit.utilities.ui.swing.component.status.*;
import com.telenav.kivakit.utilities.ui.swing.graphics.color.KivaKitColors;
import com.telenav.kivakit.utilities.ui.swing.graphics.font.Fonts;
import com.telenav.kivakit.utilities.ui.swing.theme.*;
import org.jetbrains.annotations.NotNull;
import org.openstreetmap.josm.gui.layer.Layer;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.Collections;

import static com.telenav.kivakit.utilities.ui.swing.component.status.StatusPanel.Display.NO_HEALTH_PANEL;

/**
 * The JOSM panel showing graph information and providing search functionality.
 *
 * @author jonathanl (shibo)
 */
public class GraphPanel extends BaseJosmPanel implements StatusDisplay
{
    static
    {
        KivaKitTheme.set(new KivaKitVanHelsingTheme());
    }

    private JTabbedPane tabbedPane;

    private ViewPanel viewPanel;

    private SearchPanel searchPanel;

    private QueryPanel queryPanel;

    private RoutingPanel routingPanel;

    private TagPanel tagPanel;

    private GraphLayer layer;

    private StatusPanel statusPanel;

    public GraphPanel(final GraphPlugin plugin)
    {
        super(plugin);

        createLayout(container(), false, Collections.emptyList());

        say(KivaKit.version() + " - " + KivaKit.build());

        SwingUtilities.invokeLater(() ->
                Components.children(this, component -> component.setFont(Fonts.component(Font.PLAIN, 12))));
    }

    public void html(final String message, final Object... arguments)
    {
        searchPanel().html(message, arguments);
    }

    @Override
    public GraphLayer layer()
    {
        return layer;
    }

    public void layer(final GraphLayer layer)
    {
        this.layer = layer;
        tagPanel().layer(layer);
    }

    public QueryPanel queryPanel()
    {
        if (queryPanel == null)
        {
            queryPanel = new QueryPanel(this);
        }
        return queryPanel;
    }

    public RoutingPanel routingPanel()
    {
        if (routingPanel == null)
        {
            routingPanel = new RoutingPanel(this);
        }
        return routingPanel;
    }

    @Override
    public void say(final Duration stayFor, final String message, final Object... arguments)
    {
        statusPanel.say(stayFor, message, arguments);
    }

    @Override
    public void say(final String message, final Object... arguments)
    {
        statusPanel.say(Duration.seconds(10), message, arguments);
    }

    public SearchPanel searchPanel()
    {
        if (searchPanel == null)
        {
            searchPanel = new SearchPanel(this);
        }
        return searchPanel;
    }

    public Vertex selectedVertex()
    {
        return layer().model().selection().selectedVertex();
    }

    @NotNull
    public JTabbedPane tabbedPane()
    {
        if (tabbedPane == null)
        {
            tabbedPane = new JTabbedPane();
            tabbedPane.setForeground(KivaKitColors.DARK_GRAY.asAwtColor());
            tabbedPane.addTab("home", searchPanel());
            tabbedPane.addTab("query", queryPanel());
            tabbedPane.addTab("view", viewPanel());
            tabbedPane.addTab("tags", tagPanel());
            tabbedPane.addTab("routing", routingPanel());
        }
        return tabbedPane;
    }

    public TagPanel tagPanel()
    {
        if (tagPanel == null)
        {
            tagPanel = new TagPanel(this);
        }
        return tagPanel;
    }

    public void text(final String message, final Object... arguments)
    {
        searchPanel().text(message, arguments);
    }

    public ViewPanel viewPanel()
    {
        if (viewPanel == null)
        {
            viewPanel = new ViewPanel(this);
        }
        return viewPanel;
    }

    @Override
    protected void onActiveLayerChanged()
    {
        tagPanel().layer(layer().activeLayer());
    }

    @Override
    protected void onLayerRemoving(final Layer layer)
    {
        if (layer() == null)
        {
            hidePanel();
        }
        else
        {
            refresh();
        }
    }

    @Override
    protected void onLayerReorder()
    {
        refresh();
    }

    @Override
    protected void onRefresh()
    {
        super.onRefresh();
        layer().forceRepaint();
    }

    private JPanel container()
    {
        final var container = new JPanel();
        container.setLayout(new BorderLayout());
        container.add(tabbedPane(), BorderLayout.CENTER);
        statusPanel = new StatusPanel(NO_HEALTH_PANEL);
        statusPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        container.add(statusPanel, BorderLayout.SOUTH);
        return container;
    }
}
