///////////////////////////////////////////////////////////////////////////////////////////////
// checkstyle-openrewrite-recipes: Automatically fix Checkstyle violations with OpenRewrite.
// Copyright (C) 2025 The Checkstyle OpenRewrite Recipes Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
///////////////////////////////////////////////////////////////////////////////////////////////

package org.checkstyle.autofix.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;

import org.checkstyle.autofix.parser.ReportParser;
import org.junit.jupiter.api.Test;

public class CustomImportOrderTest extends AbstractRecipeTestSupport {

    @Override
    protected String getSubpackage() {
        return "customimportorder";
    }

    @Test
    void metadata() {
        final CustomImportOrder recipe = new CustomImportOrder(Collections.emptyList(), null);
        assertEquals("CustomImportOrder recipe",
                recipe.getDisplayName());
        assertEquals("Fixes Checkstyle CustomImportOrder violations by reordering imports.",
                recipe.getDescription());
    }

    @RecipeTest
    void basicGroupOrdering(ReportParser parser) throws Exception {
        verify(parser, "BasicGroupOrdering");
    }

    @RecipeTest
    void separateLineBetweenGroups(ReportParser parser) throws Exception {
        verify(parser, "SeparateLineBetweenGroups");
    }

    @RecipeTest
    void sortAlphabetically(ReportParser parser) throws Exception {
        verify(parser, "SortAlphabetically");
    }
}
