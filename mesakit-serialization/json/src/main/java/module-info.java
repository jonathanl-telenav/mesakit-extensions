open module mesakit.serialization.json
{
    requires transitive kivakit.serialization.json;
    requires transitive mesakit.graph.core;
    requires transitive kivakit.component;

    exports com.telenav.mesakit.serialization.json;
    exports com.telenav.mesakit.serialization.json.serializers;
}
