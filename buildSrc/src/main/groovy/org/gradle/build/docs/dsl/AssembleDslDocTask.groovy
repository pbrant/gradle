/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.gradle.build.docs.dsl

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.InputFiles
import org.w3c.dom.Document
import groovy.xml.dom.DOMCategory
import org.w3c.dom.Element
import org.gradle.api.tasks.InputDirectory
import org.gradle.build.docs.XIncludeAwareXmlProvider
import org.gradle.build.docs.BuildableDOMCategory
import org.gradle.build.docs.dsl.model.ClassMetaData

/**
 * Generates the docbook source for the DSL documentation. Uses meta-data extracted from the source, meta-data about the
 * plugins, plus a docbook template file.
 */
class AssembleDslDocTask extends DefaultTask {
    @InputFile
    File sourceFile
    @InputFile
    File classMetaDataFile
    @InputFile
    File pluginsMetaDataFile
    @InputDirectory
    File classDocbookDir
    @OutputFile
    File destFile
    @InputFiles
    FileCollection classpath;

    @TaskAction
    def transform() {
        XIncludeAwareXmlProvider provider = new XIncludeAwareXmlProvider(classpath)
        provider.parse(sourceFile)
        transformDocument(provider.document)
        provider.write(destFile)
    }

    private def transformDocument(Document document) {
        use(DOMCategory) {
            use(BuildableDOMCategory) {
                Map<String, ClassMetaData> classes = loadClassMetaData()
                Map<String, ExtensionMetaData> extensions = loadPluginsMetaData()
                ClassMetaDataRepository classRepository = new ClassMetaDataRepository(classes)
                DslModel model = new DslModel(classDocbookDir, document, classpath, classRepository, extensions)
                def root = document.documentElement
                root.section[0].table.each { Element table ->
                    insertTypes(table, model)
                }
            }
        }
    }

    def loadPluginsMetaData() {
        XIncludeAwareXmlProvider provider = new XIncludeAwareXmlProvider(classpath)
        provider.parse(pluginsMetaDataFile)
        Map<String, ExtensionMetaData> extensions = [:]
        provider.root.plugin.each { Element plugin ->
            def description = plugin.'@description'
            plugin.extends.each { Element e ->
                def targetClass = e.'@targetClass'
                def extensionClass = e.'@extensionClass'
                def extension = extensions[targetClass]
                if (!extension) {
                    extension = new ExtensionMetaData(targetClass)
                    extensions[targetClass] = extension
                }
                extension.add(description, extensionClass)
            }
        }
        return extensions
    }

    def loadClassMetaData() {
        Map<String, ClassMetaData> classes;
        classMetaDataFile.withInputStream { InputStream instr ->
            ObjectInputStream ois = new ObjectInputStream(instr) {
                @Override protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) {
                    return AssembleDslDocTask.classLoader.loadClass(objectStreamClass.name)
                }

            }
            classes = ois.readObject()
        }
        return classes
    }

    def insertTypes(Element typeTable, DslModel model) {
        typeTable['@role'] = 'dslTypes'
        typeTable.addFirst {
            thead {
                tr {
                    td('Type')
                    td('Description')
                }
            }
        }

        typeTable.tr.each { Element tr ->
            insertType(tr, model)
        }
    }

    def insertType(Element tr, DslModel model) {
        String className = tr.td[0].text().trim()
        ClassDoc classDoc = model.getClassDoc(className)
        Element root = tr.ownerDocument.documentElement

        root << classDoc.classSection

        tr.children = {
            td {
                link(linkend: classDoc.id, classDoc.classSimpleName)
            }
            td(classDoc.description)
        }
    }
}
