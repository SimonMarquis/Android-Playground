# `:data:licenses`

<!--region graph-->
```mermaid
---
config:
  layout: elk
  elk:
    nodePlacementStrategy: SIMPLE
---
graph TB
  subgraph :data
    :data:licenses[licenses]:::jvm
  end
  subgraph :domain
    :domain:licenses[licenses]:::jvm
  end
  subgraph :core
    :core:di[di]:::jvm
  end

  :data:licenses -.-> :core:di
  :data:licenses ---> :domain:licenses

classDef android-application fill:#2C4162,stroke:#fff,stroke-width:2px,color:#fff;
classDef android-library fill:#3BD482,stroke:#fff,stroke-width:2px,color:#fff;
classDef android-test fill:#3BD482,stroke:#fff,stroke-width:2px,color:#fff;
classDef jvm fill:#7F52FF,stroke:#fff,stroke-width:2px,color:#fff;
```
[✨ View in `mermaid.live`](https://mermaid.live/view#base64:eyJjb2RlIjoiLS0tXG5jb25maWc6XG4gIGxheW91dDogZWxrXG4gIGVsazpcbiAgICBub2RlUGxhY2VtZW50U3RyYXRlZ3k6IFNJTVBMRVxuLS0tXG5ncmFwaCBUQlxuICBzdWJncmFwaCA6ZGF0YVxuICAgIDpkYXRhOmxpY2Vuc2VzW2xpY2Vuc2VzXTo6Omp2bVxuICBlbmRcbiAgc3ViZ3JhcGggOmRvbWFpblxuICAgIDpkb21haW46bGljZW5zZXNbbGljZW5zZXNdOjo6anZtXG4gIGVuZFxuICBzdWJncmFwaCA6Y29yZVxuICAgIDpjb3JlOmRpW2RpXTo6Omp2bVxuICBlbmRcblxuICA6ZGF0YTpsaWNlbnNlcyAtLi0-IDpjb3JlOmRpXG4gIDpkYXRhOmxpY2Vuc2VzIC0tLT4gOmRvbWFpbjpsaWNlbnNlc1xuXG5jbGFzc0RlZiBhbmRyb2lkLWFwcGxpY2F0aW9uIGZpbGw6IzJDNDE2MixzdHJva2U6I2ZmZixzdHJva2Utd2lkdGg6MnB4LGNvbG9yOiNmZmY7XG5jbGFzc0RlZiBhbmRyb2lkLWxpYnJhcnkgZmlsbDojM0JENDgyLHN0cm9rZTojZmZmLHN0cm9rZS13aWR0aDoycHgsY29sb3I6I2ZmZjtcbmNsYXNzRGVmIGFuZHJvaWQtdGVzdCBmaWxsOiMzQkQ0ODIsc3Ryb2tlOiNmZmYsc3Ryb2tlLXdpZHRoOjJweCxjb2xvcjojZmZmO1xuY2xhc3NEZWYganZtIGZpbGw6IzdGNTJGRixzdHJva2U6I2ZmZixzdHJva2Utd2lkdGg6MnB4LGNvbG9yOiNmZmY7In0=)
<!--endregion-->
