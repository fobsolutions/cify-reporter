package io.cify.views.common

import io.cify.views.BasePage
import org.apache.commons.io.FileUtils

/**
 * Created by FOB Solutions
 * Generates stacktrace page
 */
class StacktracePage {

    static String generateStacktrace(String header, String details, String body, String url, List<byte[]> embeddings, List<String> writings) {
        String stacktraceString = FileUtils.readFileToString(BasePage.stacktraceHeadTemplate)

        // Header
        if (header.isEmpty()) {
            stacktraceString = stacktraceString.replace("{isStacktraceHeadHidden}", "hide")
        } else {
            stacktraceString = stacktraceString.replace("{isStacktraceHeadHidden}", "show")
        }
        stacktraceString = stacktraceString.replace("{stacktraceHeader}", header)
        stacktraceString = stacktraceString.replace("{stacktraceDetail}", details)

        // Body
        if (body.isEmpty()) {
            stacktraceString = stacktraceString.replace("{stacktraceBody}", "")
        } else {
            String source = "<pre>{stacktraceBody}</pre>"
            source = source.replace("{stacktraceBody}", body)
            stacktraceString = stacktraceString.replace("{stacktraceBody}", source)
        }

        // link
        if (!url.isEmpty()) {
            stacktraceString = stacktraceString.replace("{stacktraceLink}", "details/" + url)
        } else {
            stacktraceString = stacktraceString.replace("{stacktraceLink}", "#link")
        }

        // embeddings
        if (embeddings.isEmpty()) {
            stacktraceString = stacktraceString.replace("{stacktraceEmbedImage}", "")
        } else {
            String embedImages = ""
            embeddings.each {
                String source = "<pre><img alt=\"Embedded Image\" src=\"{stacktraceEmbeddeding}\" /></pre>"
                embedImages = embedImages + source.replace("{stacktraceEmbeddeding}", it.toString())
            }
            stacktraceString = stacktraceString.replace("{stacktraceEmbedImage}", embedImages)
        }

        // writings
        if (writings.isEmpty()) {
            stacktraceString = stacktraceString.replace("{stacktraceWritings}", "")
        } else {
            String writingsString = ""
            writings.each {
                String source = "<pre>{stacktraceWriting}</pre>"
                writingsString = writingsString + source.replace("{stacktraceWriting}", it)
            }
            stacktraceString = stacktraceString.replace("{stacktraceEmbedImage}", writingsString)
        }
        return stacktraceString
    }
}
