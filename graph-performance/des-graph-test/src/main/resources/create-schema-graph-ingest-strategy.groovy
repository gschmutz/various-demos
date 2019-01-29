schema.clear()

schema.propertyKey('name').Text().ifNotExists().create()
schema.propertyKey('id').Text().ifNotExists().create()
schema.propertyKey('language').Text().ifNotExists().create()
schema.propertyKey('createdAt').Timestamp().ifNotExists().create()
schema.propertyKey('type').Text().ifNotExists().create()
schema.propertyKey('location').Point().ifNotExists().create()
schema.propertyKey('verified').Boolean().ifNotExists().create()
schema.propertyKey('timestamp').Timestamp().ifNotExists().create()

// vertex with generated IDs
schema.vertexLabel('twitterUser').properties('id','name','language','createdAt','verified').ifNotExists().create()
schema.vertexLabel('term').properties('id','name','type').ifNotExists().create()
schema.vertexLabel('tweet').properties('id','timestamp', 'language').ifNotExists().create()

// vertex with generated Custom IDs
schema.vertexLabel('twitterUserCV').partitionKey('id').properties('id','name','language','createdAt','verified').ifNotExists().create()
schema.vertexLabel('termCV').partitionKey('name').properties('name','type').ifNotExists().create()
schema.vertexLabel('tweetCV').partitionKey('id').properties('id','timestamp', 'language').ifNotExists().create()

schema.edgeLabel('publishes').properties('timestamp')
			.connection('twitterUser','tweet')
			.connection('twitterUserCV','tweetCV')
		.create()
schema.edgeLabel('uses').properties('type','timestamp')
			.connection('tweet','term')
			.connection('tweetCV','termCV')
		.create()
		
// ID index for generated IDs
schema.vertexLabel('twitterUser').index('twitterUserIdIdx').materialized().by('id').ifNotExists().add()
schema.vertexLabel('tweet').index('tweetUserIdIdx').materialized().by('id').ifNotExists().add()
schema.vertexLabel('term').index('nameAndTypeIdx').materialized().by('id').ifNotExists().add()

// secondary index (non identifiying)
schema.vertexLabel('twitterUser').index('twitterUserNameIdx').materialized().by('name').ifNotExists().add()
schema.vertexLabel('twitterUser').index("usesAtTimestamp").outE("publishes").by("timestamp").ifNotExists().add()
schema.vertexLabel('twitterUserCV').index('twitterUserCVNameIdx').materialized().by('name').ifNotExists().add()
schema.vertexLabel('twitterUserCV').index("usesAtTimestampCV").outE("publishes").by("timestamp").ifNotExists().add()
schema.vertexLabel('term').index('termIdx').materialized().by('name').ifNotExists().add()

// Schema description
// Use to check that the schema is built as desired
schema.describe()

