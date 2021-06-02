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

package com.telenav.mesakit.tools.applications.pbf.analyzer;

import com.telenav.kivakit.application.Application;
import com.telenav.kivakit.commandline.ArgumentParser;
import com.telenav.kivakit.commandline.SwitchParser;
import com.telenav.kivakit.data.compression.codecs.huffman.character.HuffmanCharacterCodec;
import com.telenav.kivakit.data.compression.codecs.huffman.string.HuffmanStringCodec;
import com.telenav.kivakit.filesystem.File;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfNode;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfRelation;
import com.telenav.mesakit.map.data.formats.pbf.model.entities.PbfWay;
import com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.RelationFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.filters.WayFilter;
import com.telenav.mesakit.map.data.formats.pbf.processing.readers.SerialPbfReader;
import com.telenav.mesakit.map.data.formats.pbf.project.DataFormatsPbfProject;

import java.util.List;
import java.util.Set;

import static com.telenav.kivakit.commandline.SwitchParser.booleanSwitchParser;
import static com.telenav.kivakit.filesystem.File.fileArgumentParser;
import static com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor.Action.ACCEPTED;
import static com.telenav.mesakit.map.data.formats.pbf.processing.PbfDataProcessor.Action.FILTERED_OUT;
import static com.telenav.mesakit.map.data.formats.pbf.processing.filters.RelationFilter.relationFilterSwitchParser;
import static com.telenav.mesakit.map.data.formats.pbf.processing.filters.WayFilter.wayFilterSwitchParser;

/**
 * Analyzes the given PBF file argument. Huffman codec output files are generated by this application.
 *
 * @author jonathanl (shibo)
 * @see HuffmanStringCodec
 * @see HuffmanCharacterCodec
 */
public class PbfAnalyzerApplication extends Application
{
    static final ArgumentParser<File> INPUT =
            fileArgumentParser("Input PBF file")
                    .required()
                    .build();

    static final SwitchParser<WayFilter> WAY_FILTER =
            wayFilterSwitchParser()
                    .required()
                    .build();

    static final SwitchParser<RelationFilter> RELATION_FILTER =
            relationFilterSwitchParser()
                    .required()
                    .build();

    static final SwitchParser<Boolean> SHOW_WARNINGS =
            booleanSwitchParser("show-warnings", "Show warnings for problems like bad turn restrictions")
                    .optional()
                    .defaultValue(false)
                    .build();

    static final SwitchParser<Boolean> COMPUTE_LENGTHS =
            booleanSwitchParser("compute-lengths", "Compute lengths by highway type")
                    .optional()
                    .defaultValue(false)
                    .build();

    public static void main(final String[] arguments)
    {
        new PbfAnalyzerApplication().run(arguments);
    }

    public PbfAnalyzerApplication()
    {
        super(DataFormatsPbfProject.get());
    }

    @Override
    protected List<ArgumentParser<?>> argumentParsers()
    {
        return List.of(INPUT);
    }

    @Override
    protected void onRun()
    {
        final var input = argument(INPUT);
        final var wayFilter = get(WAY_FILTER);
        final var relationFilter = get(RELATION_FILTER);

        final Analyzer analyzer = new Analyzer(commandLine());

        final var reader = listenTo(new SerialPbfReader(input));
        reader.process(new PbfDataProcessor()
        {
            @Override
            public Action onNode(final PbfNode node)
            {
                analyzer.addNode(node);
                return ACCEPTED;
            }

            @Override
            public Action onRelation(final PbfRelation relation)
            {
                if (relationFilter.accepts(relation))
                {
                    analyzer.addRelation(relation);
                    return ACCEPTED;
                }
                return FILTERED_OUT;
            }

            @Override
            public Action onWay(final PbfWay way)
            {
                if (wayFilter.accepts(way))
                {
                    analyzer.addWay(way);
                    return ACCEPTED;
                }
                return FILTERED_OUT;
            }
        });

        analyzer.report();
    }

    @Override
    protected Set<SwitchParser<?>> switchParsers()
    {
        return Set.of
                (
                        WAY_FILTER,
                        RELATION_FILTER,
                        SHOW_WARNINGS,
                        COMPUTE_LENGTHS,
                        QUIET
                );
    }
}
