namespace Smart_X.backend.Models;

/// <summary>
/// One node in a device deployment hierarchy: Facility -> Zone -> Sub-Zone.
/// A node can hold sensor IDs deployed at that level and any number of child nodes.
/// </summary>
public class DeploymentNode
{
    public string Name { get; set; } = string.Empty;
    public string Level { get; set; } = string.Empty; // expected: "Facility", "Zone", or "Sub-Zone"
    public List<string> SensorIds { get; set; } = new();
    public List<DeploymentNode> Children { get; set; } = new();
}

public static class DeploymentTreeValidator
{
    // Expected level name at each depth: depth 0 = Facility, 1 = Zone, 2 = Sub-Zone.
    private static readonly string[] ExpectedLevels = { "Facility", "Zone", "Sub-Zone" };

    /// <summary>
    /// Validates an entire deployment tree starting from a root (depth 0, should be a Facility).
    /// Returns true if no problems were found; errors describes every problem encountered,
    /// collected across the whole tree rather than stopping at the first one.
    /// </summary>
    public static bool Validate(DeploymentNode root, out List<string> errors)
    {
        errors = new List<string>();
        ValidateRecursive(root, depth: 0, path: root.Name, errors);
        return errors.Count == 0;
    }

    private static void ValidateRecursive(DeploymentNode node, int depth, string path, List<string> errors)
    {
        // --- base case 1: malformed node ---
        if (string.IsNullOrWhiteSpace(node.Name))
        {
            errors.Add($"Node at '{path}' (depth {depth}) is missing a name.");
        }

        // --- base case 2: tree nests deeper than Facility > Zone > Sub-Zone allows ---
        if (depth >= ExpectedLevels.Length)
        {
            errors.Add($"'{path}' exceeds the maximum nesting depth (Facility > Zone > Sub-Zone only).");
            return; // stop recursing down this branch — it's already invalid
        }

        // Level name at this depth should match what the hierarchy expects.
        if (!string.Equals(node.Level, ExpectedLevels[depth], StringComparison.OrdinalIgnoreCase))
        {
            errors.Add($"'{path}' is at depth {depth} and should be level '{ExpectedLevels[depth]}', but was '{node.Level}'.");
        }

        // A Sub-Zone (deepest allowed level) shouldn't have further children.
        if (depth == ExpectedLevels.Length - 1 && node.Children.Count > 0)
        {
            errors.Add($"'{path}' is a Sub-Zone and cannot contain further child zones.");
        }

        // Duplicate sensor IDs within the same node.
        var duplicateSensorIds = node.SensorIds
            .GroupBy(id => id, StringComparer.OrdinalIgnoreCase)
            .Where(g => g.Count() > 1)
            .Select(g => g.Key);
        foreach (var dup in duplicateSensorIds)
            errors.Add($"'{path}' lists sensor '{dup}' more than once.");

        // --- recursive case: validate each child, one level deeper ---
        foreach (var child in node.Children)
        {
            var childPath = string.IsNullOrWhiteSpace(child.Name) ? $"{path}/?" : $"{path}/{child.Name}";
            ValidateRecursive(child, depth + 1, childPath, errors);
        }
    }

    /// <summary>Recursively counts every sensor across the whole tree — a second, simpler
    /// example of the same recursive-descent pattern used for validation.</summary>
    public static int CountSensors(DeploymentNode node)
        => node.SensorIds.Count + node.Children.Sum(CountSensors);
}
