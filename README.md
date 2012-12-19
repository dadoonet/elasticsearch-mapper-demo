Mapper Demo Type for ElasticSearch
==================================

The mapper demo plugin illustrates how to code your own mapper plugins with Elasticsearch.

In order to install the plugin, simply run: `bin/plugin -install elasticsearch/elasticsearch-mapper-demo/1.0.0`.

    -----------------------------------------
    | Demo Mapper Plugin | ElasticSearch    |
    -----------------------------------------
    | master             | 0.20 -> master   |
    -----------------------------------------
    | 1.0.0              | 0.20             |
    -----------------------------------------


The `demo` type allows to index different a field in two ways:

- the field itself
- its uppercase value

The `demo` type is provided as a plugin extension. The plugin is a simple zip file that can be downloaded and placed under `$ES_HOME/plugins` location. It will be automatically detected and the `demo` type will be added.

Using the demo type is simple, in your mapping JSON, simply set a certain JSON element as demo, for example:

    {
        "person" : {
            "properties" : {
                "my_demo" : { "type" : "demo" }
            }
        }
    }

In this case, the JSON to index can be:

    {
        "my_demo" : "this is a lowercase sentence"
    }

The `demo` type not only indexes the content of the original field, but also automatically adds a new field with your content in uppercase under the `demo` property : `my_demo.demo`.

You can control how you will index it using mappings. For example:

    {
        "person" : {
            "properties" : {
                "my_demo" : {
                    "type" : "demo",
                    "fields" : {
                        "demo" : {"index" : "not_analyzed"}
                    }
                }
            }
        }
    }

In the above example, the actual uppercase content indexed is mapped under `fields` name `demo`, and we decide not to analyze it.

License
-------

    This software is licensed under the Apache 2 license, quoted below.

    Copyright 2012 David Pilato

    Licensed under the Apache License, Version 2.0 (the "License"); you may not
    use this file except in compliance with the License. You may obtain a copy of
    the License at

        http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
    License for the specific language governing permissions and limitations under
    the License.
