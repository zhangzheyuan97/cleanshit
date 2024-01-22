<!DOCTYPE html>
<html>
<head>
    <@cloud.mui/>
</head>
<body>
    <iframe id="authIframe" width="100%" height="100%"></iframe>
</body>
<script>
    $(function () {
        var userId = "${userId}";
        var businessId = "${businessId}";
        var token = "${token}";
        document.getElementById("authIframe").src = "/portal/#/?userId=" + userId + "&token=" + token;
        setTimeout(()=>{
            document.getElementById("authIframe").src = "/bmc-page-config/preview/APPROVE_VIEW?uid=" + businessId;
        },500);
    })
</script>
</html>