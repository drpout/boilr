# Contributing to Boilr

## Adding a translation

### Via Weblate
1. Just go to [Boilr @ Hosted Weblate](https://hosted.weblate.org/projects/boilr/) and start editing. Tip: use Machine translation to ease your work.

### Via git (for coders)
0. Fork Boilr's repository.
1. Create a directory on [src/main/res](/src/main/res) named values-xx where xx is the [ISO 639-1](https://en.wikipedia.org/wiki/List_of_ISO_639-1_codes) language code.
2. Copy the strings.xml file from any other values-xx directory to your new directory.
3. Translate the strings inside strings.xml. You should use the original English strings from [values/strings.xml](/src/main/res/values/strings.xml) as reference. Note: values/strings.xml has more entries than all other values-xx/strings.xml since some strings should not be translated.
4. Commit, push, open a pull request and profit.

### xliff:g tag
Do not put the `<xliff:g>` tag on translated strings. This tag should only appear in source language (English). It is used to help translators by wrapping descriptions and examples for format arguments. In translated strings put just the format argument that is inside the `<xliff:g>` tag. Example:

Source string: `Couldn\'t retrieve pairs for <xliff:g id="exchange" example="Bitstamp">%1$s</xliff:g>.`

Translated string: `Imposible obtener pares para %1$s.`
