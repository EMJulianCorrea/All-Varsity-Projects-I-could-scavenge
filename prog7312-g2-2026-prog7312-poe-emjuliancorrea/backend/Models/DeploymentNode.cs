namespace Smart_X.backend.Models;

public class DeploymentNode
{
    public string Name { get; set; } = string.Empty;
    public string Level { get; set; } = string.Empty;
    public List<string> SensorIds { get; set; } = new();
    public List<DeploymentNode> Children { get; set; } = new();
}

public static class DeploymentTreeValidator
{
    private static readonly string[] ExpectedLevels = { "Facility", "Zone", "Sub-Zone" };

    public static bool Validate(DeploymentNode root, out List<string> errors)
    {
        errors = new List<string>();
        ValidateRecursive(root, depth: 0, path: root.Name, errors);
        return errors.Count == 0;
    }

    private static void ValidateRecursive(DeploymentNode node, int depth, string path, List<string> errors)
    {
        if (string.IsNullOrWhiteSpace(node.Name))
        {
            errors.Add($"Node at '{path}' (depth {depth}) is missing a name.");
        }

        if (depth >= ExpectedLevels.Length)
        {
            errors.Add($"'{path}' exceeds the maximum nesting depth (Facility > Zone > Sub-Zone only).");
            return;
        }

        if (!string.Equals(node.Level, ExpectedLevels[depth], StringComparison.OrdinalIgnoreCase))
        {
            errors.Add($"'{path}' is at depth {depth} and should be level '{ExpectedLevels[depth]}', but was '{node.Level}'.");
        }

        if (depth == ExpectedLevels.Length - 1 && node.Children.Count > 0)
        {
            errors.Add($"'{path}' is a Sub-Zone and cannot contain further child zones.");
        }

        var duplicateSensorIds = node.SensorIds
            .GroupBy(id => id, StringComparer.OrdinalIgnoreCase)
            .Where(g => g.Count() > 1)
            .Select(g => g.Key);
        foreach (var dup in duplicateSensorIds)
            errors.Add($"'{path}' lists sensor '{dup}' more than once.");
        foreach (var child in node.Children)
        {
            var childPath = string.IsNullOrWhiteSpace(child.Name) ? $"{path}/?" : $"{path}/{child.Name}";
            ValidateRecursive(child, depth + 1, childPath, errors);
        }
    }

    public static int CountSensors(DeploymentNode node)
        => node.SensorIds.Count + node.Children.Sum(CountSensors);
}
