
JIRA.bind(JIRA.Events.NEW_CONTENT_ADDED, function() {
    AJS.$(".mlcs-container").undelegate().delegate("select", "change", function(e) {
        var eventSource = AJS.$(this);
        var parentValue = eventSource.val();
        eventSource.nextAll("select").addClass("hidden").attr("disabled", true);
        AJS.$('select[data-parent="' + parentValue + '"]').removeClass("hidden").removeAttr("disabled").trigger("change");
    });
});
