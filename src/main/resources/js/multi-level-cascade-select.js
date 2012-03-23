

JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function (e, context) {
    AJS.$(".mlcs-container").delegate("select", "change", function() {
        var eventSource = AJS.$(this);
        var parentValue = eventSource.val();
        eventSource.nextAll("select").addClass("hidden").attr("disabled", true);
        AJS.$('select[data-parent="' + parentValue + '"]').removeClass("hidden").removeAttr("disabled").trigger("change");
    });
});
