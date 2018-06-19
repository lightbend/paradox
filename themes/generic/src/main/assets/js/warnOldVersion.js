function initOldVersionWarnings($, thisVersion, projectUrl) {
    var schemeLessUrl = projectUrl;
    if (projectUrl.startsWith("http://")) projectUrl = schemeLessUrl.substring(5);
    else if (projectUrl.startsWith("https://")) projectUrl = schemeLessUrl.substring(6);
    const url = schemeLessUrl + "/paradox.json";
    console.log("trying to read version data from " + url);
    $.get(url, function (versionData) {
        const currentVersion = versionData.version;
        if (thisVersion !== currentVersion) {
            showVersionWarning(thisVersion, currentVersion, projectUrl);
        }
    });
}

function showVersionWarning(thisVersion, currentVersion, projectUrl) {
    $('#version-warning').prepend(
        '<div id="floaty-warning" class="warning"/>' +
        '<p><span style="font-weight: bold">This documentation regards version ' + thisVersion + ', ' +
        'however the current version is <a href="' + projectUrl + '">' + currentVersion + '</a>.</span></p>' +
        '</div>'
    );
}